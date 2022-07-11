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

package com.megaease.easeagent.metrics.jvm.memory;

import com.megaease.easeagent.metrics.MetricProviderImplTest;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.MetricType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import org.junit.Test;

import static org.junit.Assert.*;

public class JVMMemoryMetricV2Test {
    private static final JVMMemoryMetricV2 JVM_MEMORY_METRIC_V_2;

    static {
        EaseAgent.configFactory = MockConfig.getPluginConfigManager();
        EaseAgent.metricRegistrySupplier = MetricProviderImplTest.METRIC_PROVIDER.metricSupplier();
        JVM_MEMORY_METRIC_V_2 = JVMMemoryMetricV2.getMetric();
    }


    @Test
    public void getMetric() {
        assertNotNull(JVM_MEMORY_METRIC_V_2);
    }

    @Test
    public void nameFactory() {
        NameFactory nameFactory = JVMMemoryMetricV2.nameFactory();
        assertEquals(1, nameFactory.metricTypes().size());
        assertTrue(nameFactory.metricTypes().contains(MetricType.GaugeType));
    }

    @Test
    public void doJob() {
        IPluginConfig config = AgentFieldReflectAccessor.getFieldValue(JVM_MEMORY_METRIC_V_2, "config");
        MetricRegistry metricRegistry = AgentFieldReflectAccessor.getFieldValue(JVM_MEMORY_METRIC_V_2, "metricRegistry");
        assertTrue(config.enabled());
        JVM_MEMORY_METRIC_V_2.doJob();
        assertTrue(metricRegistry.getMetrics().size() > 0);
    }
}
