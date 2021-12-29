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

import com.megaease.easeagent.metrics.MetricProvider;
import com.megaease.easeagent.metrics.jvm.gc.JVMGCMetricV2;
import com.megaease.easeagent.metrics.jvm.memory.JVMMemoryMetricV2;
import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.utils.MockProvider;

public class MetricProviderMock implements MockProvider {
    private static final MetricProvider METRIC_PROVIDER = new MetricProvider();
    private static final JVMGCMetricV2 JVMGC_METRIC_V_2;
    private static final JVMMemoryMetricV2 JVM_MEMORY_METRIC_V_2;

    static {
        METRIC_PROVIDER.setConfig(ConfigMock.getCONFIGS());
        METRIC_PROVIDER.setAgentReport(ReportMock.getAgentReport());
        JVMGC_METRIC_V_2 = METRIC_PROVIDER.jvmGcMetricV2();
        JVM_MEMORY_METRIC_V_2 = METRIC_PROVIDER.jvmMemoryMetricV2();
    }


    public static MetricProvider getMetricProvider() {
        return METRIC_PROVIDER;
    }

    public static JVMGCMetricV2 getJvmgcMetricV2() {
        return JVMGC_METRIC_V_2;
    }

    public static JVMMemoryMetricV2 getJvmMemoryMetricV2() {
        return JVM_MEMORY_METRIC_V_2;
    }

    @Override
    public Object get() {
        return METRIC_PROVIDER;
    }
}
