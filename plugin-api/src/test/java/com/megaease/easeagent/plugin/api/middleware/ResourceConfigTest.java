/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.api.middleware;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import static org.junit.Assert.*;

public class ResourceConfigTest {
    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Test
    public void getResourceConfig() {
        String env = "TEST_RESOURCE";
        environmentVariables.set(env, "{\"uris\":\"127.0.0.1:9092\"}");
        ResourceConfig resourceConfig = ResourceConfig.getResourceConfig(env, true);
        assertEquals("127.0.0.1:9092", resourceConfig.getUris());
        assertNotNull(resourceConfig.getFirstHostAndPort());
        assertEquals("127.0.0.1", resourceConfig.getFirstHostAndPort().getHost());
        assertEquals(9092, (int) resourceConfig.getFirstHostAndPort().getPort());
        assertEquals(1, resourceConfig.getHostAndPorts().size());
        assertEquals(1, resourceConfig.getUriList().size());
        assertEquals("127.0.0.1:9092", resourceConfig.getFirstUri());


        resourceConfig = ResourceConfig.getResourceConfig(env, false);
        assertEquals("127.0.0.1:9092", resourceConfig.getUris());
        assertNull(resourceConfig.getFirstHostAndPort());

        environmentVariables.set(env, "{\"uris\":\"127.0.0.1:9092,127.0.0.1:9093\"}");
        resourceConfig = ResourceConfig.getResourceConfig(env, true);
        assertEquals("127.0.0.1:9092,127.0.0.1:9093", resourceConfig.getUris());
        assertNotNull(resourceConfig.getFirstHostAndPort());
        assertEquals("127.0.0.1", resourceConfig.getFirstHostAndPort().getHost());
        assertEquals(9092, (int) resourceConfig.getFirstHostAndPort().getPort());
        assertEquals(2, resourceConfig.getHostAndPorts().size());
        assertEquals("127.0.0.1", resourceConfig.getHostAndPorts().get(1).getHost());
        assertEquals(9093, (int) resourceConfig.getHostAndPorts().get(1).getPort());

        assertEquals(2, resourceConfig.getUriList().size());
        assertEquals("127.0.0.1:9092", resourceConfig.getFirstUri());
        assertEquals("127.0.0.1:9093", resourceConfig.getUriList().get(1));


        environmentVariables.set(env, null);
    }
}
