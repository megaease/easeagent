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

package com.megaease.easeagent.context.log;

import com.megaease.easeagent.log4j2.MDC;
import com.megaease.easeagent.log4j2.api.AgentLoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Logger;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class LoggerFactoryImplTest {
    LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();

    @Test
    public void getLogger() {
        Logger logger = loggerFactory.getLogger(LoggerFactoryImplTest.class.getName());
        logger.info("aaaa");
        MDC.put("testMdc", "testMdc_value");
        logger.info("bbbb");
        assertNotNull(MDC.get("testMdc"));
    }

    @Test
    public void factory() {
        AgentLoggerFactory<LoggerImpl> agentLoggerFactory = loggerFactory.factory();
        assertNotNull(agentLoggerFactory);
        LoggerImpl logger = agentLoggerFactory.getLogger(LoggerFactoryImplTest.class.getName());
        logger.info("aaaa");
        agentLoggerFactory.mdc().put("newFactory", "newFactory");
        assertNotNull(MDC.get("newFactory"));

    }

    @Test
    public void build() {
        LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
        assertNotNull(loggerFactory);
    }
}
