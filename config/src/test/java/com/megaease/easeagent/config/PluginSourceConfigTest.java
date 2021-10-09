package com.megaease.easeagent.config;

import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class PluginSourceConfigTest {

    Map<String, String> getSource(String id) {
        Map<String, String> properties = PluginConfigTest.globalSource();
        Map<String, String> s = new HashMap<>();
        for (Map.Entry<String, String> pEntry : properties.entrySet()) {
            s.put("plugin.testDomain.testNamespace." + id + "." + pEntry.getKey(), pEntry.getValue());
        }
        return s;
    }


    Map<String, String> selfSource() {
        return getSource("self");
    }


    PluginSourceConfig buildImpl() {
        String domain = "testDomain";
        String namespace = "testNamespace";
        Map<String, String> source = selfSource();
        source.putAll(getSource("kafka"));
        source.putAll(getSource("mq"));
        source.put("plugin.testDomain.testssss.self.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        return PluginSourceConfig.build(domain, namespace, source);
    }

    @Test
    public void build() {
        buildImpl();
    }

    @Test
    public void getSource() {
        Map<String, String> source = selfSource();
        source.putAll(getSource("kafka"));
        source.putAll(getSource("mq"));
        assertEquals(buildImpl().getSource(), source);
    }

    @Test
    public void getDomain() {
        assertEquals(buildImpl().getDomain(), "testDomain");
    }

    @Test
    public void getNamespace() {
        assertEquals(buildImpl().getNamespace(), "testNamespace");
    }

    @Test
    public void getIds() {
        Set<String> ids = new HashSet<>();
        ids.add("self");
        ids.add("kafka");
        ids.add("mq");
        assertEquals(buildImpl().getIds(), ids);
    }

    @Test
    public void getProperties() {

        assertEquals(buildImpl().getProperties("self"), PluginConfigTest.globalSource());
        assertEquals(buildImpl().getProperties("kafka"), PluginConfigTest.globalSource());
        assertEquals(buildImpl().getProperties("mq"), PluginConfigTest.globalSource());
        assertEquals(buildImpl().getProperties("selfss"), new HashMap<>());
    }
}
