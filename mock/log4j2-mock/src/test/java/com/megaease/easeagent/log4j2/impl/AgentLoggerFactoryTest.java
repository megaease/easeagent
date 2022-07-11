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

package com.megaease.easeagent.log4j2.impl;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.log4j2.MDC;
import com.megaease.easeagent.log4j2.api.AgentLogger;
import com.megaease.easeagent.log4j2.api.AgentLoggerFactory;
import com.megaease.easeagent.mock.log4j2.URLClassLoaderSupplier;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AgentLoggerFactoryTest {

    @Test
    public void builder() throws NoSuchMethodException, IllegalAccessException, InstantiationException, NoSuchFieldException, InvocationTargetException, ClassNotFoundException {
        try {
            AgentLoggerFactory.builder(() -> null, AgentLogger.LOGGER_SUPPLIER, AgentLogger.class).build();
            assertTrue("must be err", false);
        } catch (Exception e) {
            assertNotNull(e);
        }
        AgentLoggerFactory<?> factory = AgentLoggerFactory.builder(new URLClassLoaderSupplier(), AgentLogger.LOGGER_SUPPLIER, AgentLogger.class).build();

    }

    @Test
    public void getLogger() {
        Logger logger = LoggerFactory.getLogger(AgentLoggerFactoryTest.class.getName());
        logger.info("aaaa");
        MDC.put("testMdc", "testMdc_value");
        logger.info("bbbb");
        assertNotNull(MDC.get("testMdc"));
    }


    @Test
    public void newFactory() {
        AgentLoggerFactory<TestAgentLogger> factory = LoggerFactory.newFactory(TestAgentLogger.LOGGER_SUPPLIER, TestAgentLogger.class);
        TestAgentLogger logger = factory.getLogger(AgentLoggerFactoryTest.class.getName());
        logger.info("aaaa");
        factory.mdc().put("newFactory", "newFactory");
        assertNotNull(MDC.get("newFactory"));

    }

    static class TestAgentLogger extends AgentLogger {
        public static final Function<java.util.logging.Logger, TestAgentLogger> LOGGER_SUPPLIER = logger -> new TestAgentLogger(logger);

        public TestAgentLogger(java.util.logging.Logger logger) {
            super(logger);
        }
    }
}
