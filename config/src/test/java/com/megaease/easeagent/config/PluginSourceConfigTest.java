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

package com.megaease.easeagent.config;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PluginSourceConfigTest {

    public static String DOMAIN = "testDomain";
    public static String NAMESPACE = "testNamespace";
    public static String TEST_TRACE_ID = "test-trace";
    public static String GLOBAL_ID = TEST_TRACE_ID;
    public static String TEST_METRIC_ID = "test-metric";
    public static String TEST_AAA_ID = "test-AAA";


    public static Map<String, String> getSource(String namespace, String id) {
        Map<String, String> properties = PluginConfigTest.globalSource();
        Map<String, String> s = new HashMap<>();
        for (Map.Entry<String, String> pEntry : properties.entrySet()) {
            s.put("plugin." + DOMAIN + "." + namespace + "." + id + "." + pEntry.getKey(), pEntry.getValue());
        }
        return s;
    }

    public static Map<String, String> getSource(String id) {
        return getSource(NAMESPACE, id);
    }


    public static Map<String, String> getGlobal() {
        return getSource("global", GLOBAL_ID);
    }

    public static Map<String, String> buildSource() {
        Map<String, String> source = getGlobal();
        source.putAll(getSource(TEST_METRIC_ID));
        source.putAll(getSource(TEST_TRACE_ID));
        source.put("plugin.testDomain.testssss.self.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        source.put("plugin.testDomain.testssss.kafka.lll", "aaa");
        return source;
    }

    PluginSourceConfig buildImpl(String namespace, String id) {

        Map<String, String> source = buildSource();
        return PluginSourceConfig.build(DOMAIN, namespace, id, source);
    }

    @Test
    public void build() {
        PluginSourceConfig config = buildImpl(NAMESPACE, TEST_TRACE_ID);
        assertNotNull(config);

    }

    @Test
    public void getSource() {
        assertEquals(buildImpl("global", GLOBAL_ID).getSource(), getGlobal());
        assertEquals(Collections.emptyMap(), buildImpl(NAMESPACE, TEST_AAA_ID).getSource());
        assertEquals(getSource(TEST_TRACE_ID), buildImpl(NAMESPACE, TEST_TRACE_ID).getSource());
        assertEquals(getSource(TEST_METRIC_ID), buildImpl(NAMESPACE, TEST_METRIC_ID).getSource());
    }

    @Test
    public void getDomain() {
        assertEquals(buildImpl(NAMESPACE, TEST_AAA_ID).getDomain(), DOMAIN);
        assertEquals(buildImpl(NAMESPACE, TEST_TRACE_ID).getDomain(), DOMAIN);
        assertEquals(buildImpl(NAMESPACE, TEST_METRIC_ID).getDomain(), DOMAIN);
    }

    @Test
    public void getNamespace() {
        assertEquals(buildImpl(NAMESPACE, TEST_AAA_ID).getNamespace(), NAMESPACE);
        assertEquals(buildImpl(NAMESPACE, TEST_TRACE_ID).getNamespace(), NAMESPACE);
        assertEquals(buildImpl(NAMESPACE, TEST_METRIC_ID).getNamespace(), NAMESPACE);
    }


    @Test
    public void getId() {
        assertEquals(buildImpl(NAMESPACE, TEST_AAA_ID).getId(), TEST_AAA_ID);
        assertEquals(buildImpl(NAMESPACE, TEST_TRACE_ID).getId(), TEST_TRACE_ID);
        assertEquals(buildImpl(NAMESPACE, TEST_METRIC_ID).getId(), TEST_METRIC_ID);
    }

    @Test
    public void getProperties() {

        assertEquals(buildImpl("global", GLOBAL_ID).getProperties(), PluginConfigTest.globalSource());
        assertEquals(buildImpl(NAMESPACE, TEST_TRACE_ID).getProperties(), PluginConfigTest.globalSource());
        assertEquals(buildImpl(NAMESPACE, TEST_METRIC_ID).getProperties(), PluginConfigTest.globalSource());
        assertEquals(buildImpl(NAMESPACE, TEST_AAA_ID).getProperties(), new HashMap<>());
    }
}
