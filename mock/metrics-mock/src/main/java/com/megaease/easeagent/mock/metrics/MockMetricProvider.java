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

package com.megaease.easeagent.mock.metrics;

import com.megaease.easeagent.metrics.AutoRefreshReporter;
import com.megaease.easeagent.metrics.MetricBeanProviderImpl;
import com.megaease.easeagent.metrics.MetricProviderImpl;
import com.megaease.easeagent.metrics.jvm.JvmBeanProvider;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.mock.utils.MockProvider;

public class MockMetricProvider implements MockProvider {
    private static final MetricBeanProviderImpl METRIC_PROVIDER = new MetricBeanProviderImpl();
    private static final JvmBeanProvider JVM_METRIC_PROVIDER = new JvmBeanProvider();

    static {
        METRIC_PROVIDER.setConfig(MockConfig.getCONFIGS());
        METRIC_PROVIDER.setAgentReport(MockReport.getAgentReport());
        JVM_METRIC_PROVIDER.afterPropertiesSet();
        MockReport.setMetricFlushable(MockMetricProvider::flush);
    }

    public static MetricBeanProviderImpl getMetricProvider() {
        return METRIC_PROVIDER;
    }

    @Override
    public Object get() {
        return getMetricProvider();
    }

    public static void flush() {
        MetricProviderImpl metricProvider = METRIC_PROVIDER.getMetricProvider();
        if (metricProvider == null) {
            return;
        }
        for (AutoRefreshReporter autoRefreshReporter : metricProvider.getReporterList()) {
            autoRefreshReporter.getReporter().report();
        }
    }

    public static void clearAll() {
        MetricProviderImpl metricProvider = METRIC_PROVIDER.getMetricProvider();
        if (metricProvider == null) {
            return;
        }
        for (com.megaease.easeagent.plugin.api.metric.MetricRegistry metricRegistry : metricProvider.getRegistryList()) {
            MetricTestUtils.clear(metricRegistry);
        }
    }
}
