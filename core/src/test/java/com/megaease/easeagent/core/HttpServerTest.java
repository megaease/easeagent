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

import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.config.PluginConfigManager;
import com.megaease.easeagent.config.WrappedConfigManager;
import com.megaease.easeagent.core.config.PluginPropertiesHttpHandler;
import com.megaease.easeagent.core.config.PluginPropertyHttpHandler;
import com.megaease.easeagent.core.config.ServiceUpdateAgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpHandler;
import com.megaease.easeagent.httpserver.nano.AgentHttpServer;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerTest {
    static WrappedConfigManager oldWrappedConfigManager;
    static IConfigFactory originConfigFactory;

    @BeforeClass
    public static void before() throws NoSuchFieldException, IllegalAccessException {
        oldWrappedConfigManager = GlobalAgentHolder.getWrappedConfigManager();
        originConfigFactory = EaseAgent.configFactory;
    }

    @AfterClass
    public static void after() throws NoSuchFieldException, IllegalAccessException {
        setWrappedConfigManager(oldWrappedConfigManager);
        EaseAgent.configFactory = originConfigFactory;
    }


    private static void setWrappedConfigManager(WrappedConfigManager wrappedConfigManager) throws NoSuchFieldException, IllegalAccessException {
        GlobalAgentHolder.setWrappedConfigManager(wrappedConfigManager);
    }

    private static String runUpHttpServer() throws Exception {
        int port = getPort();
        return runUpHttpServer(port);
    }

    private static int getPort() throws IOException {
        Random random = new Random();
        for (int i = 0; i < 4; i++) {
            int port = 8000 + random.nextInt(1000);
            if (!isPortUsing(port)) {
                return port;
            }
        }
        throw new RuntimeException("can not found port for test.");
    }

    private static boolean isPortUsing(int port) throws UnknownHostException {
        boolean flag = false;
        InetAddress theAddress = InetAddress.getLocalHost();
        try {
            new Socket(theAddress, port);
            flag = true;
        } catch (IOException e) {
        }
        return flag;
    }

    private static String runUpHttpServer(int port) {
        String httpServer = "http://127.0.0.1:" + port;
        AgentHttpServer agentHttpServer = new AgentHttpServer(port);
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new ServiceUpdateAgentHttpHandler());
        list.add(new PluginPropertyHttpHandler());
        list.add(new PluginPropertiesHttpHandler());
        agentHttpServer.addHttpRoutes(list);
        agentHttpServer.startServer();
        return httpServer;
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

        GlobalConfigs configs = new GlobalConfigs(source);
        IConfigFactory iConfigFactory = PluginConfigManager.builder(configs).build();
        AtomicInteger count = new AtomicInteger(0);

        iConfigFactory.getConfig("observability", "kafka", "kafka-tracings")
            .addChangeListener(new PluginConfigChange(count, "kafka.kafka-tracings"));
        iConfigFactory.getConfig("observability", "kafka", "tracings")
            .addChangeListener(new PluginConfigChange(count, "kafka.tracings"));
        iConfigFactory.getConfig("observability", "kafka", "metric")
            .addChangeListener(new PluginConfigChange(count, "kafka.metric"));

        WrappedConfigManager cfgMng = null;
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            cfgMng = new WrappedConfigManager(customClassLoader, configs);
            setWrappedConfigManager(cfgMng);
        } catch (Exception e) {
            System.out.println("" + e.getMessage());
        }
        Assert.assertNotNull(cfgMng);

        EaseAgent.configFactory = iConfigFactory;

        String httpServer = runUpHttpServer();
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

        IPluginConfig config = AutoRefreshPluginConfigRegistry.getOrCreate("observability", "kafka", "metric");
        Assert.assertTrue(config.enabled());

        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/enabled/false/1");

        Assert.assertFalse(config.enabled());

        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metric/properties/enabled/true/1");
        get(httpServer + "/plugins/domains/observability/namespaces/global/metric/properties/enabled/false/1");
        Assert.assertFalse(config.enabled());

        get(httpServer + "/plugins/domains/observability/namespaces/global/metric/properties/enabled/true/1");
        resp = post(httpServer + "/config", "{\"observability.metrics.jdbcConnection.enabled\":\"false\",\"observability.metrics.kafka.enabled\": \"false\", \"version\": \"1\"}");
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

    static class PluginConfigChange implements PluginConfigChangeListener {
        final AtomicInteger count;
        final String name;

        public PluginConfigChange(AtomicInteger count, String name) {
            this.count = count;
            this.name = name;
        }

        @Override
        public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
            count.incrementAndGet();
            System.out.printf("----------------------- on change %s begin ----------------------%n", name);
            System.out.println("old config:");
            printConfig(oldConfig);
            System.out.println("\n-------------------------------\n");
            System.out.println("new config:");
            printConfig(newConfig);
            System.out.printf("----------------------- on change %s end ---------------------- \n\n%n", name);
        }

        public void printConfig(IPluginConfig config) {
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
