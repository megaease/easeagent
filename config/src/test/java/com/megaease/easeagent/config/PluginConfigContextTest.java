package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class PluginConfigContextTest {


    PluginConfigContext build() {
        return build(PluginSourceConfigTest.buildSource());
    }

    PluginConfigContext build(Map<String, String> source) {
        Configs configs = new Configs(source);
        return PluginConfigContext.builder(configs).build();
    }

    @Test
    public void builder() {
        build();
    }

    @Test
    public void getConfig() {
        PluginConfigContext pluginConfigContext = build();
        for (Map.Entry<String, String> entry : PluginSourceConfigTest.buildSource().entrySet()) {
            assertEquals(pluginConfigContext.getConfig(entry.getKey()), entry.getValue());
        }
    }

    private void checkPluginConfigString(PluginConfig pluginConfig, Map<String, String> source) {
        for (Map.Entry<String, String> entry : source.entrySet()) {
            assertEquals(pluginConfig.getString(entry.getKey()), entry.getValue());
        }
    }

    public static Map<String, String> buildSource() {
        Map<String, String> source = getSource("trace", "self", PluginConfigTest.globalSource());
        source.putAll(getSource("kafka", "self", PluginConfigTest.globalSource()));
        source.putAll(getSource("kafka", "trace", PluginConfigTest.coverSource()));
        source.put("plugin.testDomain.testssss.self.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        return source;
    }


    public static Map<String, String> getSource(String namespace, String id, Map<String, String> properties) {
        Map<String, String> s = new HashMap<>();
        for (Map.Entry<String, String> pEntry : properties.entrySet()) {
            s.put("plugin.testDomain." + namespace + "." + id + "." + pEntry.getKey(), pEntry.getValue());
        }
        return s;
    }

    @Test
    public void getConfig1() {
        PluginConfigContext pluginConfigContext = build();
        PluginConfig pluginConfig = pluginConfigContext.getConfig("testDomain", "testNamespace", "self");
        checkPluginConfigString(pluginConfig, PluginConfigTest.globalSource());
        pluginConfig = pluginConfigContext.getConfig("testDomain", "testNamespace", "kafka");
        checkPluginConfigString(pluginConfig, PluginConfigTest.globalSource());
        pluginConfig = pluginConfigContext.getConfig("testDomain", "testNamespace", "mq");
        checkPluginConfigString(pluginConfig, PluginConfigTest.globalSource());

        Map<String, String> source = buildSource();
        source.putAll(getSource("trace", "kafka", PluginConfigTest.coverSource()));
        try {
            pluginConfigContext = build(source);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertNotNull(e);
        }

        Configs configs = new Configs(buildSource());
        pluginConfigContext = PluginConfigContext.builder(configs).build();
        final PluginConfig pluginConfig1 = pluginConfigContext.getConfig("testDomain", "kafka", "trace");
        final AtomicReference<Config> oldPluginConfig = new AtomicReference<>();
        final AtomicReference<Config> newPluginConfig = new AtomicReference<>();
        PluginConfigTest.checkAllType(pluginConfig1);
        pluginConfig1.addChangeListener((oldConfig, newConfig) -> {
            oldPluginConfig.set(oldConfig);
            newPluginConfig.set(newConfig);
        });

        configs.updateConfigs(Collections.singletonMap("plugin.testDomain.kafka.trace.enabled", "false"));
        assertNotNull(oldPluginConfig.get());
        assertNotNull(newPluginConfig.get());
        assertTrue(oldPluginConfig.get() == pluginConfig1);
        assertTrue(((PluginConfig) oldPluginConfig.get()).getConfigChangeListener() == pluginConfig1.getConfigChangeListener());
        assertTrue(oldPluginConfig.get().getBoolean("enabled"));
        assertFalse(newPluginConfig.get().getBoolean("enabled"));
        PluginConfigTest.checkAllType((PluginConfig) oldPluginConfig.get());

        configs.updateConfigs(Collections.singletonMap("plugin.testDomain.kafka.trace.enabled", "true"));
        assertFalse(oldPluginConfig.get().getBoolean("enabled"));
        assertTrue(newPluginConfig.get().getBoolean("enabled"));


        configs.updateConfigs(Collections.singletonMap("plugin.testDomain.trace.self.enabled", "false"));
        assertTrue(oldPluginConfig.get().getBoolean("enabled"));
        assertFalse(newPluginConfig.get().getBoolean("enabled"));


        Config newConfig = newPluginConfig.get();
        configs.updateConfigs(Collections.singletonMap("plugin.testDomain.kafka.self.enabled", "false"));
        assertTrue(newPluginConfig.get() == newConfig);
    }

    @Test
    public void shutdown() {
    }
}
