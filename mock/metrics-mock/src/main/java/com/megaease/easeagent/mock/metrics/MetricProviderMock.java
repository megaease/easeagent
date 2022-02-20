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

package com.megaease.easeagent.mock.metrics;

import com.megaease.easeagent.metrics.MetricBeanProvider;
import com.megaease.easeagent.metrics.jvm.JvmBeanProvider;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetricV2;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetricV2;
import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.utils.MockProvider;

public class MetricProviderMock implements MockProvider {
    private static final MetricBeanProvider METRIC_PROVIDER = new MetricBeanProvider();
    private static final JvmBeanProvider JVM_METRIC_PROVIDER = new JvmBeanProvider();

    static {
        METRIC_PROVIDER.setConfig(ConfigMock.getCONFIGS());
        METRIC_PROVIDER.setAgentReport(ReportMock.getAgentReport());
        JVM_METRIC_PROVIDER.afterPropertiesSet();
    }

    public static MetricBeanProvider getMetricProvider() {
        return METRIC_PROVIDER;
    }

    @Override
    public Object get() {
        return getMetricProvider();
    }
}
