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
import com.megaease.easeagent.core.utils.WrappedConfigManager;
import com.megaease.easeagent.httpserver.AgentHttpHandler;
import com.megaease.easeagent.httpserver.AgentHttpServer;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
import java.util.concurrent.atomic.AtomicInteger;

public class HttpServerTest {
    WrappedConfigManager oldWrappedConfigManager;

    @Before
    public void before() throws NoSuchFieldException, IllegalAccessException {
        Field field = Bootstrap.class.getDeclaredField("wrappedConfigManager");
        field.setAccessible(true);
        oldWrappedConfigManager = (WrappedConfigManager) field.get(null);
        field.setAccessible(false);
    }

    @After
    public void after() throws NoSuchFieldException, IllegalAccessException {
        setWrappedConfigManager(oldWrappedConfigManager);
    }


    private void setWrappedConfigManager(WrappedConfigManager wrappedConfigManager) throws NoSuchFieldException, IllegalAccessException {
        Field field = Bootstrap.class.getDeclaredField("wrappedConfigManager");
        field.setAccessible(true);
        field.set(null, wrappedConfigManager);
        field.setAccessible(false);
    }


    @Test
    public void httpServer() throws Exception {
        HashMap<String, String> source = new HashMap<>();
        source.put("plugin.observability.global.metrics.enabled", "true");
        source.put("plugin.observability.global.tracings.enabled", "true");

        source.put("plugin.observability.global.kafka-tracings.enabled", "true");
        source.put("plugin.observability.global.kafka-tracings.size", "12");

        source.put("plugin.observability.kafka.kafka-tracings.size", "13");
        source.put("plugin.observability.kafka.tracings.enabled", "true");
        source.put("plugin.observability.kafka.tracings.servicePrefix", "true");
        source.put("plugin.observability.kafka.metrics.enabled", "true");
        source.put("plugin.observability.kafka.metrics.interval", "30");

        source.put("plugin.observability.kafka.metrics.topic", "platform-meter");
        source.put("plugin.observability.kafka.metrics.appendType", "kafka");

        Configs configs = new Configs(source);
        IConfigFactory iConfigFactory = PluginConfigManager.builder(configs).build();
        AtomicInteger count = new AtomicInteger(0);
///plugins/domains/observability/namespaces/kafka/kafka-tracings/properties/enabled/true/1
        iConfigFactory.getConfig("observability", "kafka", "kafka-tracings").addChangeListener(new ConfigChange(count, "kafka.kafka-tracings"));
        iConfigFactory.getConfig("observability", "kafka", "tracings").addChangeListener(new ConfigChange(count, "kafka.tracings"));
        iConfigFactory.getConfig("observability", "kafka", "metrics").addChangeListener(new ConfigChange(count, "kafka.metrics"));
        try {
            ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
            setWrappedConfigManager(new WrappedConfigManager(customClassLoader, configs));
        } catch (Exception e) {
            System.out.println("" + e.getMessage());
        }

        DatagramSocket s = new DatagramSocket(0);
        int port = s.getLocalPort();
        String httpServer = "http://127.0.0.1:" + port;
        System.out.println("run up http server : " + httpServer);
        AgentHttpServer agentHttpServer = new AgentHttpServer(port);
        List<AgentHttpHandler> list = new ArrayList<>();
        list.add(new Bootstrap.PluginPropertyHttpHandler());
        list.add(new Bootstrap.PluginPropertiesHttpHandler());
        agentHttpServer.addHttpRoutes(list);
        agentHttpServer.startServer();

        Thread.sleep(100);
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties/interval/15/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties/interval/14/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties/interval/13/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties/interval/12/1");
        Assert.assertEquals(count.get(), 4);
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/kafka-tracings/properties/enabled/false/1");
        get(httpServer + "/plugins/domains/observability/namespaces/kafka/kafka-tracings/properties/enabled/true/1");
        Assert.assertEquals(count.get(), 6);
        get(httpServer + "/plugins/domains/observability/namespaces/global/tracings/properties/enabled/false/1");
        get(httpServer + "/plugins/domains/observability/namespaces/global/tracings/properties/enabled/true/1");
        Assert.assertEquals(count.get(), 8);
        post(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties", "{\"enabled\":\"false\",\"interval\": 15, \"version\": \"1\"}");
        post(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties", "{\"enabled\":\"true\",\"interval\": 15, \"version\": \"1\"}");
        post(httpServer + "/plugins/domains/observability/namespaces/kafka/metrics/properties", "{\"enabled\":\"true\",\"interval\": 13, \"version\": \"1\"}");
        Assert.assertEquals(count.get(), 11);
//        Thread.sleep(TimeUnit.HOURS.toMillis(1));
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
            System.out.println(String.format("----------------------- on change %s begin ----------------------", name));
            System.out.println("old config:");
            printConfig(oldConfig);
            System.out.println("\n-------------------------------\n");
            System.out.println("new config:");
            printConfig(newConfig);
            System.out.println(String.format("----------------------- on change %s end ---------------------- \n\n", name));
        }

        public void printConfig(Config config) {
            for (String s : config.keySet()) {
                String value = config.getString(s);
                if (value != null && "TRUE".equals(value.toUpperCase())) {
                    System.out.println(String.format("%s=%s", s, config.getBoolean(s)));
                } else {
                    System.out.println(String.format("%s=%s", s, config.getString(s)));
                }
            }
        }
    }
}
