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

package com.megaease.easeagent.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.context.log.LoggerFactoryImpl;
import com.megaease.easeagent.context.log.LoggerMdc;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class GlobalContextTest {
    GlobalContext globalContext;

    @Before
    public void before() {
        Map<String, String> initConfigs = new HashMap<>();
        initConfigs.put("name", "demo-service");
        initConfigs.put("system", "demo-system");
        Configs configs = new Configs(initConfigs);
        LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
        globalContext = new GlobalContext(configs, NoOpMetrics.NO_OP_METRIC_SUPPLIER, loggerFactory, new LoggerMdc(loggerFactory.factory().mdc()));
    }


    @Test
    public void getConf() {
        assertNotNull(globalContext.getConf());
    }

    @Test
    public void getMdc() {
        assertNotNull(globalContext.getMdc());
    }

    @Test
    public void getLoggerFactory() {
        assertNotNull(globalContext.getLoggerFactory());
    }

    @Test
    public void getMetric() {
        assertNotNull(globalContext.getMetric());
    }
}
