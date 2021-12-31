/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.core;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.PluginConfigManager;
import com.megaease.easeagent.core.config.PluginPropertiesHttpHandler;
import com.megaease.easeagent.core.config.PluginPropertyHttpHandler;
import com.megaease.easeagent.core.config.ServiceUpdateAgentHttpHandler;
import com.megaease.easeagent.core.config.WrappedConfigManager;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.plugin.api.config.AutoRefreshRegistry;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.junit.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerTest {
    static WrappedConfigManager oldWrappedConfigManager;
    static IConfigFactory originConfigFactory;

    @BeforeClass
    public static void before() throws NoSuchFieldException, IllegalAccessException {
        Field field = Bootstrap.class.getDeclaredField("wrappedConfigManager");
        field.setAccessible(true);
        oldWrappedConfigManager = (WrappedConfigManager) field.get(null);
        field.setAccessible(false);

        originConfigFactory = EaseAgent.configFactory;
    }

    @AfterClass
    public static void after() throws NoSuchFieldException, IllegalAccessException {
        setWrappedConfigManager(oldWrappedConfigManager);
        EaseAgent.configFactory = originConfigFactory;
    }


    private static void setWrappedConfigManager(WrappedConfigManager wrappedConfigManager) throws NoSuchFieldException, IllegalAccessException {
        Field field = Bootstrap.class.getDeclaredField("wrappedConfigManager");
        field.setAccessible(true);
        field.set(null, wrappedConfigManager);
        field.setAccessible(false);
    }


    @Test
    public void httpServer() throws Exception {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metric.enabled", "true");
        source.put("plugin.observability.global.tracings.enabled", "true");

        source.put("plugin.observability.global.kafka-tracings.enabled", "true");
        source.put("plugin.observability.global.kafka-tracings.size", "12");

        source.put("plugin.observability.kafka.kafka-tracings.size", "13");
        source.put("plugin.observability.kafka.tracings.enabled", "true");
        source.put("plugin.observability.kafka.tracings.servicePrefix", "true");
        source.put("plugin.observability.kafka.metric.enabled", "true");
        source.put("plugin.observability.kafka.metric.interval", "30");

        source.put("plugin.observability.kafka.metric.topic", "platform-meter");
        source.put("plugin.observability.kafka.metric.appendType", "kafka");

        Configs configs = new Configs(source);
        IConfigFactory iConfigFactory = PluginConfigManager.builder(configs).build();
        AtomicInteger count = new AtomicInteger(0);

        iConfigFactory.getConfig("observability", "kafka", "kafka-tracings")
            .addChangeListener(new ConfigChange(count, "kafka.kafka-tracings"));
        iConfigFactory.getConfig("observability", "kafka", "tracings")
            .addChangeListener(new ConfigChange(count, "kafka.tracings"));
        iConfigFactory.getConfig("observability", "kafka", "metric")
            .addChangeListener(new ConfigChange(count, "kafka.metric"));

        WrappedConfigManager cfgMng = null;
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
             cfgMng = new WrappedConfigManager(customClassLoader, configs);
            setWrappedConfigManager(cfgMng);
            GlobalAgentHolder.setWrappedConfigManager(cfgMng);
        } catch (Exception e) {
            System.out.println("" + e.getMessage());
        }
        Assert.assertNotNull(cfgMng);

        EaseAgent.configFactory = iConfigFactory;

        DatagramSocket s = new DatagramSocket(0);
        int port = s.getLocalPort();
        String httpServer = "http://127.0.0.1:" + port;
        System.out.println("run up http server : " + httpServer);
        AgentHttpServer agentHttpServer = new AgentHttpServer(port);
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new ServiceUpdateAgentHttpHandler());
        list.add(new PluginPropertyHttpHandler());
        list.add(new PluginPropertiesHttpHandler());
        agentHttpServer.addHttpRoutes(list);
        agentHttpServer.startServer();

        Thread.sleep(100);
        String resp;
        resp = get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/interval/15/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/interval/14/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/interval/13/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/interval/12/1");
        Assert.assertEquals(4, count.get());
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/kafka-tracings/properties/enabled/false/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/kafka-tracings/properties/enabled/true/1");
        Assert.assertEquals(6, count.get());
        get(httpServer + "/plugins/domains/observability/namespaces/global/tracings/properties/enabled/false/1");
        get(httpServer + "/plugins/domains/observability/namespaces/global/tracings/properties/enabled/true/1");
        Assert.assertEquals(8, count.get());
        post(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties", "{\"enabled\":\"false\",\"interval\": 15, \"version\": \"1\"}");
        post(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties", "{\"enabled\":\"true\",\"interval\": 15, \"version\": \"1\"}");
        post(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties", "{\"enabled\":\"true\",\"interval\": 13, \"version\": \"1\"}");
        Assert.assertEquals(11, count.get());

        Config config = AutoRefreshRegistry.getOrCreate("observability", "kafka", "metric");
        Assert.assertTrue(config.enabled());

        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/enabled/false/1");

        Assert.assertFalse(config.enabled());

        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/enabled/true/1");
        get(httpServer + "/plugins/domains/observability/namespaces/global/metric/properties/enabled/false/1");
        Assert.assertFalse(config.enabled());

        get(httpServer + "/plugins/domains/observability/namespaces/global/metric/properties/enabled/true/1");
        resp = post(httpServer + "/config-service", "{\"observability.metrics.jdbcConnection.enabled\":\"false\",\"observability.metrics.kafka.enabled\": \"false\", \"version\": \"1\"}");
        System.out.println(resp);

        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
    }

    static String get(String urlStr) throws IOException {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }

    static String post(String urlStr, String body) throws IOException {
        String charset = "UTF-8";
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestProperty("Accept-Charset", charset);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        try (OutputStream output = conn.getOutputStream()) {
            output.write(body.getBytes(charset));
            output.flush();
        }

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(conn.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line);
            }
        }
        return result.toString();
    }

    static class ConfigChange implements ConfigChangeListener {
        final AtomicInteger count;
        final String name;

        public ConfigChange(AtomicInteger count, String name) {
            this.count = count;
            this.name = name;
        }

        @Override
        public void onChange(Config oldConfig, Config newConfig) {
            count.incrementAndGet();
            System.out.printf("----------------------- on change %s begin ----------------------%n", name);
            System.out.println("old config:");
            printConfig(oldConfig);
            System.out.println("\n-------------------------------\n");
            System.out.println("new config:");
            printConfig(newConfig);
            System.out.printf("----------------------- on change %s end ---------------------- \n\n%n", name);
        }

        public void printConfig(Config config) {
            for (String s : config.keySet()) {
                String value = config.getString(s);
                if ("TRUE".equalsIgnoreCase(value)) {
                    System.out.printf("%s=%s%n", s, config.getBoolean(s));
                } else {
                    System.out.printf("%s=%s%n", s, config.getString(s));
                }
            }
        }
    }
}
