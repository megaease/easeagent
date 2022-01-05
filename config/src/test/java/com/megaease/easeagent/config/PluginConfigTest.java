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

import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class PluginConfigTest {

    public static Map<String, String> globalSource() {
        Map<String, String> global = new HashMap<>();
        global.put("enabled", "true");
        global.put("tcp.enabled", "true");
        global.put("host", "127.0.0.1");
        global.put("count", "127");
        global.put("double", "127.1");
        global.put("double_1", "127.2");
        global.put("list", "a,b,c");
        return global;
    }

    public static Map<String, String> coverSource() {
        Map<String, String> cover = new HashMap<>();
        cover.put("tcp.enabled", "false");
        cover.put("http.enabled", "true");
        cover.put("host", "127.0.0.3");
        cover.put("count", "127");
        cover.put("double", "127.3");
        cover.put("list", "a,b,c");
        return cover;
    }

    PluginConfig build() {
        String domain = "testdomain";
        String id = "testid";
        String namespace = "NAMESPACE";
        Map<String, String> global = globalSource();
        Map<String, String> cover = coverSource();
        return PluginConfig.build(domain, id, global, namespace, cover, null);
    }

    @Test
    public void domain() {
        assertEquals("testdomain", build().domain());
    }

    @Test
    public void namespace() {
        assertEquals("NAMESPACE", build().namespace());
    }

    @Test
    public void id() {
        assertEquals("testid", build().id());
    }

    @Test
    public void hasProperty() {
        checkHasProperty(build());

    }

    public static void checkHasProperty(PluginConfig config) {
        assertTrue(config.hasProperty("enabled"));
        assertTrue(config.hasProperty("tcp.enabled"));
        assertTrue(config.hasProperty("http.enabled"));
        assertFalse(config.hasProperty("http.enabled.cccc"));
    }

    @Test
    public void getString() {
        checkString(build());
    }

    public static void checkString(PluginConfig config) {
        assertEquals("true", config.getString("enabled"));
        assertEquals("false", config.getString("tcp.enabled"));
        assertEquals("127", config.getString("count"));
        assertEquals("127.0.0.3", config.getString("host"));
        assertEquals("true", config.getString("http.enabled"));
        assertEquals(null, config.getString("http.enabled.sss"));
    }

    @Test
    public void getInt() {
        checkInt(build());
    }

    public static void checkInt(PluginConfig config) {
        assertEquals(127, (int) config.getInt("count"));
        assertNull(config.getInt("enabled"));
        assertNull(config.getInt("cccccccccccccc"));
    }


    @Test
    public void getBoolean() {
        checkBoolean(build());
    }

    public static void checkBoolean(PluginConfig config) {
        assertTrue(config.getBoolean("enabled"));
        assertFalse(config.getBoolean("tcp.enabled"));
        assertFalse(config.getBoolean("http.enabled"));
        assertFalse(config.getBoolean("http.enabled.ssss"));
    }

    @Test
    public void getDouble() {
        checkDouble(build());
    }


    public static void checkDouble(PluginConfig config) {
        assertTrue(Math.abs(config.getDouble("double") - 127.3) < 0.0001);
        assertTrue(Math.abs(config.getDouble("double_1") - 127.2) < 0.0001);
        assertNull(config.getDouble("enabled"));
    }

    @Test
    public void getLong() {
        checkLong(build());
    }

    public static void checkLong(PluginConfig config) {
        assertEquals(127l, (long) config.getLong("count"));
        assertNull(config.getLong("enabled"));
        assertNull(config.getLong("cccccccccccccc"));
    }

    @Test
    public void getStringList() {
        checkStringList(build());
    }


    public static void checkStringList(PluginConfig config) {
        List<String> list = config.getStringList("list");
        assertEquals(3, list.size());
        assertEquals("a", list.get(0));
        assertEquals("b", list.get(1));
        assertEquals("c", list.get(2));
    }


    @Test
    public void addChangeListener() {
        PluginConfig config = build();
        config.addChangeListener((oldConfig, newConfig) -> {
        });
        AtomicInteger count = new AtomicInteger(0);
        config.foreachConfigChangeListener(new Consumer<PluginConfigChangeListener>() {
            @Override
            public void accept(PluginConfigChangeListener listener) {
                count.incrementAndGet();
            }
        });
        assertEquals(1, count.get());
    }

    @Test
    public void getConfigChangeListener() {
        addChangeListener();
    }

    @Test
    public void keySet() {
        checkKeySet(build());
    }

    public static void checkKeySet(PluginConfig config) {
        Set<String> set = config.keySet();
        Map<String, String> source = globalSource();
        source.putAll(coverSource());
        assertEquals(set.size(), source.size());
        for (String s : set) {
            assertTrue(source.containsKey(s));
        }
    }


    public static void checkAllType(PluginConfig config) {
        checkHasProperty(config);
        checkString(config);
        checkBoolean(config);
        checkInt(config);
        checkLong(config);
        checkDouble(config);
        checkStringList(config);
        checkKeySet(config);
    }


}
