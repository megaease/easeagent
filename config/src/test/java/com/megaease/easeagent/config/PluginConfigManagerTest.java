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

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class PluginConfigManagerTest {
    public static String DOMAIN = "testDomain";
    public static String NAMESPACE = "testNamespace";
    public static String TEST_TRACE_ID = "test-trace";
    public static String GLOBAL_ID = TEST_TRACE_ID;
    public static String TEST_METRIC_ID = "test-metric";
    public static String TEST_AAA_ID = "test-AAA";


    PluginConfigManager build() {
        return build(PluginSourceConfigTest.buildSource());
    }

    PluginConfigManager build(Map<String, String> source) {
        Configs configs = new Configs(source);
        return PluginConfigManager.builder(configs).build();
    }

    @Test
    public void testBuild() {
        build();
        assertTrue(true);
    }

    @Test
    public void getConfig() {
        PluginConfigManager pluginConfigManager = build();
        for (Map.Entry<String, String> entry : PluginSourceConfigTest.buildSource().entrySet()) {
            assertEquals(pluginConfigManager.getConfig(entry.getKey()), entry.getValue());
        }
    }

    private void checkPluginConfigString(PluginConfig pluginConfig, Map<String, String> source) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
            assertEquals(pluginConfig.getString(entry.getKey()), entry.getValue());
        }
    }

    public static Map<String, String> buildSource() {
        Map<String, String> source = getSource("global", GLOBAL_ID, PluginConfigTest.globalSource());
        source.putAll(getSource("global", TEST_AAA_ID, PluginConfigTest.globalSource()));
        source.putAll(getSource(NAMESPACE, TEST_TRACE_ID, PluginConfigTest.coverSource()));
        source.put("plugin.testDomain.testssss.self.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        return source;
    }


    public static Map<String, String> getSource(String namespace, String id, Map<String, String> properties) {
        Map<String, String> s = new HashMap<>();
        for (Map.Entry<String, String> pEntry : properties.entrySet()) {
            s.put("plugin." + DOMAIN + "." + namespace + "." + id + "." + pEntry.getKey(), pEntry.getValue());
        }
        return s;
    }

    @Test
    public void getConfig1() throws InterruptedException {
        PluginConfigManager pluginConfigManager = build();
        PluginConfig pluginConfig = pluginConfigManager.getConfig(PluginSourceConfigTest.DOMAIN, "global", PluginSourceConfigTest.GLOBAL_ID);
        checkPluginConfigString(pluginConfig, PluginConfigTest.globalSource());
        pluginConfig = pluginConfigManager.getConfig(PluginSourceConfigTest.DOMAIN, PluginSourceConfigTest.NAMESPACE, PluginSourceConfigTest.TEST_TRACE_ID);
        checkPluginConfigString(pluginConfig, PluginConfigTest.globalSource());
        pluginConfig = pluginConfigManager.getConfig(PluginSourceConfigTest.DOMAIN, PluginSourceConfigTest.NAMESPACE, PluginSourceConfigTest.TEST_METRIC_ID);
        checkPluginConfigString(pluginConfig, PluginConfigTest.globalSource());

        Map<String, String> source = buildSource();
        source.putAll(getSource("trace", TEST_TRACE_ID, PluginConfigTest.coverSource()));
        source.putAll(getSource("trace", GLOBAL_ID, PluginConfigTest.coverSource()));
        build(source);

        Configs configs = new Configs(buildSource());
        pluginConfigManager = PluginConfigManager.builder(configs).build();
        final PluginConfig pluginConfig1 = pluginConfigManager.getConfig(DOMAIN, NAMESPACE, TEST_TRACE_ID);
        final AtomicReference<IPluginConfig> oldPluginConfig = new AtomicReference<>();
        final AtomicReference<IPluginConfig> newPluginConfig = new AtomicReference<>();
        PluginConfigTest.checkAllType(pluginConfig1);
        pluginConfig1.addChangeListener((oldConfig, newConfig) -> {
            oldPluginConfig.set(oldConfig);
            newPluginConfig.set(newConfig);
        });

        configs.updateConfigs(Collections.singletonMap(String.format("plugin.%s.%s.%s.enabled", DOMAIN, NAMESPACE, TEST_TRACE_ID), "false"));
        assertNotNull(oldPluginConfig.get());
        assertNotNull(newPluginConfig.get());
        assertTrue(oldPluginConfig.get() == pluginConfig1);
        final AtomicReference<PluginConfigChangeListener> oldPluginConfigListener = new AtomicReference<>();
        final AtomicReference<PluginConfigChangeListener> newPluginConfigListener = new AtomicReference<>();
        ((PluginConfig) oldPluginConfig.get()).foreachConfigChangeListener(listener -> oldPluginConfigListener.set(listener));
        ((PluginConfig) newPluginConfig.get()).foreachConfigChangeListener(listener -> newPluginConfigListener.set(listener));
        assertTrue(oldPluginConfigListener.get() == newPluginConfigListener.get());
        assertTrue(oldPluginConfig.get().getBoolean("enabled"));
        assertFalse(newPluginConfig.get().getBoolean("enabled"));
        PluginConfigTest.checkAllType((PluginConfig) oldPluginConfig.get());

        configs.updateConfigs(Collections.singletonMap(String.format("plugin.%s.%s.%s.enabled", DOMAIN, NAMESPACE, TEST_TRACE_ID), "true"));
        assertFalse(oldPluginConfig.get().getBoolean("enabled"));
        assertTrue(newPluginConfig.get().getBoolean("enabled"));


        configs.updateConfigs(Collections.singletonMap(String.format("plugin.%s.global.%s.enabled", DOMAIN, TEST_TRACE_ID), "false"));
        assertTrue(oldPluginConfig.get().getBoolean("enabled"));
        assertFalse(newPluginConfig.get().getBoolean("enabled"));


        IPluginConfig newConfig = newPluginConfig.get();
        configs.updateConfigs(Collections.singletonMap(String.format("plugin.%s.global.%s.enabled", DOMAIN, TEST_AAA_ID), "false"));
        assertTrue(newPluginConfig.get() == newConfig);

        configs.updateConfigs(Collections.singletonMap(String.format("ssss.%s.global.%s.enabled", DOMAIN, TEST_AAA_ID), "false"));
    }

}
