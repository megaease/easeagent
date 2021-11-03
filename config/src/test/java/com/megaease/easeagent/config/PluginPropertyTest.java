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

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class PluginPropertyTest {

    public PluginProperty build() {
        return new PluginProperty("testdomain", "testnamespace", "testId", "testproperty");
    }

    @Test
    public void getDomain() {
        assertEquals(build().getDomain(), "testdomain");
    }

    @Test
    public void getNamespace() {
        assertEquals(build().getNamespace(), "testnamespace");
    }

    @Test
    public void getId() {
        assertEquals(build().getId(), "testId");
    }

    @Test
    public void getProperty() {
        assertEquals(build().getProperty(), "testproperty");
    }

    @Test
    public void testEquals() {
        assertTrue(build().equals(build()));
        PluginProperty property = new PluginProperty("testdomain", "testnamespace", "testId", "testproperty1");
        assertFalse(build().equals(property));
        Map<PluginProperty, String> pluginPropertyStringMap = new HashMap<>();
        pluginPropertyStringMap.put(build(), "testValue");
        assertTrue(pluginPropertyStringMap.containsKey(build()));
        assertEquals(pluginPropertyStringMap.get(build()), "testValue");
        assertFalse(pluginPropertyStringMap.containsKey(property));
    }

    @Test
    public void testHashCode() {
        assertEquals(build().hashCode(), build().hashCode());
        PluginProperty property = new PluginProperty("testdomain", "testnamespace", "testId", "testproperty1");
        assertNotEquals(build().hashCode(), property.hashCode());
    }
}
