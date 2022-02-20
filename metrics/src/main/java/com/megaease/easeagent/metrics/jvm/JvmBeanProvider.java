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

package com.megaease.easeagent.metrics.jvm;

import com.megaease.easeagent.metrics.MetricProviderImpl;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetricV2;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetricV2;
import com.megaease.easeagent.plugin.bean.AgentInitializingBean;
import com.megaease.easeagent.plugin.bean.BeanProvider;

public class JvmBeanProvider implements BeanProvider, AgentInitializingBean {
    private final MetricProviderImpl metricProvider = new MetricProviderImpl();

    public void jvmGcMetricV2() {
        JVMGCMetricV2.getMetric();
    }

    public void jvmMemoryMetricV2() {
        JVMMemoryMetricV2.getMetric();
    }

    @Override
    public int order() {
        return BeanOrder.METRIC_REGISTRY.getOrder();
    }

    @Override
    public void afterPropertiesSet() {
        jvmGcMetricV2();
        jvmMemoryMetricV2();
    }
}
