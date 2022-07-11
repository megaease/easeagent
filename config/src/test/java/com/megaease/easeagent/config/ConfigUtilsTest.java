/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.Config;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class ConfigUtilsTest {
    @Test
    public void test_bindProp() throws Exception {
        Configs configs = new Configs(Collections.singletonMap("hello", "world"));
        String[] rst = new String[]{null};
        ConfigUtils.bindProp("hello", configs, Config::getString, v -> rst[0] = v);
        Assert.assertEquals("world", rst[0]);
        configs.updateConfigs(Collections.singletonMap("hello", "test"));
        Assert.assertEquals("test", rst[0]);
        configs.updateConfigs(Collections.singletonMap("hello", "one"));
        Assert.assertEquals("one", rst[0]);
    }

    @Test
    public void test_json2KVMap() throws Exception {
        Map<String, String> map = ConfigUtils.json2KVMap("{\n" +
            "  \"output\": {\n" +
            "    \"servers\": \"127.0.0.1\",\n" +
            "    \"timeout\": 1000,\n" +
            "    \"enabled\": true,\n" +
            "    \"arr\": [\"x\", { \"test\": 0 }]\n" +
            "  },\n" +
            "  \"hello\":null,\n" +
            "  \"metrics\": {\n" +
            "    \"obj\": {\n" +
            "      \"a\": 1,\n" +
            "      \"b\": \"2\",\n" +
            "      \"c\": false\n" +
            "    },\n" +
            "    \"request\": {\n" +
            "      \"topic\": \"hello\",\n" +
            "      \"enabled\": false\n" +
            "    }\n" +
            "  }\n" +
            "}");
        Assert.assertEquals("127.0.0.1", map.get("output.servers"));
        Assert.assertEquals("1000", map.get("output.timeout"));
        Assert.assertEquals("true", map.get("output.enabled"));
        Assert.assertEquals("x", map.get("output.arr.0"));
        Assert.assertEquals("0", map.get("output.arr.1.test"));
        Assert.assertEquals("", map.get("hello"));
        Assert.assertEquals("1", map.get("metrics.obj.a"));
        Assert.assertEquals("2", map.get("metrics.obj.b"));
        Assert.assertEquals("false", map.get("metrics.obj.c"));
        Assert.assertEquals("hello", map.get("metrics.request.topic"));
        Assert.assertEquals("false", map.get("metrics.request.enabled"));
    }

    @Test
    public void test_json2KVMap_2() throws IOException {
        Map<String, String> map = ConfigUtils.json2KVMap("{\"serviceHeaders\":{\"mesh-app-backend\":[\"X-canary\"]}}");
        Assert.assertEquals("X-canary", map.get("serviceHeaders.mesh-app-backend.0"));
    }

    @Test
    public void isGlobal() {
        Assert.assertTrue(ConfigUtils.isGlobal("global"));
        Assert.assertFalse(ConfigUtils.isGlobal("globaldf"));
    }

    @Test
    public void isPluginConfig() {
        Assert.assertTrue(ConfigUtils.isPluginConfig("plugin."));
        Assert.assertFalse(ConfigUtils.isPluginConfig("plugin"));
        Assert.assertFalse(ConfigUtils.isPluginConfig("plugins."));
        Assert.assertTrue(ConfigUtils.isPluginConfig("plugin.observability.kafka.kafka-trace", "observability", "kafka", "kafka-trace"));
        Assert.assertFalse(ConfigUtils.isPluginConfig("plugin.observability.kafka.kafka-trace", "observabilitys", "kafka", "kafka-trace"));
        Assert.assertFalse(ConfigUtils.isPluginConfig("plugin.observability.kafka.kafka-trace", "observability", "kafkas", "kafka-trace"));
        Assert.assertFalse(ConfigUtils.isPluginConfig("plugin.observability.kafka.kafka-trace", "observability", "kafka", "kafka-traces"));
    }

    @Test
    public void pluginProperty() {
        PluginProperty pluginProperty = ConfigUtils.pluginProperty("plugin.observability.kafka.self.enabled");
        Assert.assertEquals("observability", pluginProperty.getDomain());
        Assert.assertEquals("kafka", pluginProperty.getNamespace());
        Assert.assertEquals("self", pluginProperty.getId());
        Assert.assertEquals("enabled", pluginProperty.getProperty());
        pluginProperty = ConfigUtils.pluginProperty("plugin.observability.kafka.self.tcp.enabled");
        Assert.assertEquals("observability", pluginProperty.getDomain());
        Assert.assertEquals("kafka", pluginProperty.getNamespace());
        Assert.assertEquals("self", pluginProperty.getId());
        Assert.assertEquals("tcp.enabled", pluginProperty.getProperty());
        try {
            ConfigUtils.pluginProperty("plugin.observability.kafka.self");
            assertTrue("must be error", false);
        } catch (Exception e) {
            Assert.assertNotNull(e);
        }


    }
}
