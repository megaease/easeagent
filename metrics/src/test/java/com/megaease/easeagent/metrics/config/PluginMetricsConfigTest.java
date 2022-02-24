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

package com.megaease.easeagent.metrics.config;

import com.megaease.easeagent.config.PluginConfig;
import com.megaease.easeagent.metrics.TestConst;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PluginMetricsConfigTest {
    PluginConfig pluginConfig;
    PluginMetricsConfig pluginMetricsConfig;

    @Before
    public void before() {
        pluginConfig = MockConfig.getPluginConfigManager().getConfig(ConfigConst.OBSERVABILITY, TestConst.NAMESPACE, ConfigConst.PluginID.METRIC);
        pluginMetricsConfig = new PluginMetricsConfig(pluginConfig);
    }

    @Test
    public void isEnabled() {
        assertTrue(pluginMetricsConfig.isEnabled());

    }

    @Test
    public void getInterval() {
        assertEquals(30, pluginMetricsConfig.getInterval());
    }

    @Test
    public void getIntervalUnit() {
        assertEquals(TimeUnit.SECONDS, pluginMetricsConfig.getIntervalUnit());

        PluginConfig pluginConfig2 = MockConfig.getPluginConfigManager().getConfig(ConfigConst.OBSERVABILITY, TestConst.NAMESPACE2, ConfigConst.PluginID.METRIC);
        PluginMetricsConfig pluginMetricsConfig2 = new PluginMetricsConfig(pluginConfig2);
        assertEquals(TimeUnit.MILLISECONDS, pluginMetricsConfig2.getIntervalUnit());
    }


    @Test
    public void setIntervalChangeCallback() {
        AtomicBoolean doit = new AtomicBoolean(false);
        assertEquals(30, pluginMetricsConfig.getInterval());
        pluginMetricsConfig.setIntervalChangeCallback(() -> {
            doit.set(true);
        });
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(TestConst.INTERVAL_CONFIG, "40"));
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(doit.get());
        assertEquals(40, pluginMetricsConfig.getInterval());
        MockConfig.getCONFIGS().updateConfigs(Collections.singletonMap(TestConst.INTERVAL_CONFIG, "30"));
        assertEquals(30, pluginMetricsConfig.getInterval());
    }
}
