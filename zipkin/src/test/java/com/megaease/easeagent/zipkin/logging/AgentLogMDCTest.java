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

package com.megaease.easeagent.zipkin.logging;

import org.junit.Test;
import org.slf4j.MDC;

import static org.junit.Assert.*;

public class AgentLogMDCTest {

    @Test
    public void create() {
        LogUtilsTest.reset();
        try (LogUtilsTest.Close ignored = LogUtilsTest.reset()) {
            ClassLoader classLoader1 = LogUtilsTest.getClassLoader(new String[]{"log4j-slf4j-impl", "log4j-core", "log4j-api"});
            assertNotNull(AgentLogMDC.create(classLoader1));
        }
        try (LogUtilsTest.Close ignored = LogUtilsTest.reset()) {
            ClassLoader classLoader2 = LogUtilsTest.getClassLoader(new String[]{"slf4j-api", "logback-core", "logback-access", "logback-classic"});
            assertNotNull(AgentLogMDC.create(classLoader2));
        }
    }

    @Test
    public void put() {
        try (LogUtilsTest.Close ignored = LogUtilsTest.reset()) {
            AgentLogMDC agentLogMDC = AgentLogMDC.create(Thread.currentThread().getContextClassLoader());
            String name = "testName";
            String value = "testValue";
            agentLogMDC.put(name, value);
            assertEquals(value, agentLogMDC.get(name));
            assertEquals(value, MDC.get(name));
            MDC.remove(name);
            assertNull(agentLogMDC.get(name));
            assertNull(MDC.get(name));
        }
    }

    @Test
    public void get() {
        put();
    }

    @Test
    public void remove() {
        put();
    }
}
