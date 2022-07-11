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

package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfig;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Type;

public class AutoRefreshConfigSupplierTest {
    @Test
    public void getType() {
        AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig> supplier = new AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig>() {
            @Override
            public TestAutoRefreshPluginConfig newInstance() {
                return new TestAutoRefreshPluginConfig();
            }
        };
        AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig> supplier2 = new AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig>() {
            @Override
            public TestAutoRefreshPluginConfig newInstance() {
                return new TestAutoRefreshPluginConfig();
            }
        };
        Assert.assertEquals(supplier.getType(), supplier2.getType());
        Type type1 = supplier.getType();
        Type type2 = supplier2.getType();
        Assert.assertTrue(type1.getTypeName().equalsIgnoreCase(TestAutoRefreshPluginConfig.class.getName()));
        System.out.println(type1.equals(type2));
    }

    @Test
    public void newInstance() {
        AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig> supplier = new AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig>() {
            @Override
            public TestAutoRefreshPluginConfig newInstance() {
                return new TestAutoRefreshPluginConfig();
            }
        };
        AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig> supplier2 = new AutoRefreshConfigSupplier<TestAutoRefreshPluginConfig>() {
            @Override
            public TestAutoRefreshPluginConfig newInstance() {
                return new TestAutoRefreshPluginConfig();
            }
        };

        Assert.assertEquals(supplier.newInstance().getClass(), supplier2.newInstance().getClass());
    }

    static class TestAutoRefreshPluginConfig implements AutoRefreshPluginConfig {

        @Override
        public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {

        }
    }
}
