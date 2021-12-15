/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfig;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;

public class AutoRefreshConfigSupplierTest {


    @Test
    public void getType() {
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier = new AutoRefreshConfigSupplier<TestAutoRefreshConfig>() {
            @Override
            public TestAutoRefreshConfig newInstance() {
                return new TestAutoRefreshConfig();
            }
        };
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier2 = new AutoRefreshConfigSupplier<TestAutoRefreshConfig>() {
            @Override
            public TestAutoRefreshConfig newInstance() {
                return new TestAutoRefreshConfig();
            }
        };
        Assert.assertEquals(supplier.getType(), supplier2.getType());
        Type type1 = supplier.getType();
        Type type2 = supplier2.getType();
        Assert.assertTrue(type1.getTypeName().equalsIgnoreCase(TestAutoRefreshConfig.class.getName()));
        System.out.println(type1.equals(type2));
    }

    @Test
    public void newInstance() {
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier = new AutoRefreshConfigSupplier<TestAutoRefreshConfig>() {
            @Override
            public TestAutoRefreshConfig newInstance() {
                return new TestAutoRefreshConfig();
            }
        };
        AutoRefreshConfigSupplier<TestAutoRefreshConfig> supplier2 = new AutoRefreshConfigSupplier<TestAutoRefreshConfig>() {
            @Override
            public TestAutoRefreshConfig newInstance() {
                return new TestAutoRefreshConfig();
            }
        };

        Assert.assertEquals(supplier.newInstance().getClass(), supplier2.newInstance().getClass());
    }

    class TestAutoRefreshConfig implements AutoRefreshConfig {

        @Override
        public void onChange(Config oldConfig, Config newConfig) {

        }


    }
}
