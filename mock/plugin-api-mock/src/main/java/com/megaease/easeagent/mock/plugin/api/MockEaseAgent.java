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

package com.megaease.easeagent.mock.plugin.api;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.metrics.MockMetricProvider;
import com.megaease.easeagent.mock.metrics.MetricTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.ContextUtils;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.mock.report.MockSpanReport;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.mock.zipkin.MockTracingProvider;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.metric.ServiceMetric;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;

import java.util.Map;
import java.util.function.Predicate;

public class MockEaseAgent {
    public static ReportSpan getLastSpan() {
        return MockReport.getLastSpan();
    }

    public static void cleanLastSpan() {
        MockReport.cleanLastSpan();
    }

    public static void setMockSpanReport(MockSpanReport mockSpanReport) {
        MockReport.setMockSpanReport(mockSpanReport);
    }

    public static void setMockMetricReport(Reporter metricReportMock) {
        MockReport.setMockMetricReport(metricReportMock);
    }

    public static LastJsonReporter lastMetricJsonReporter(Predicate<Map<String, Object>> filter) {
        return MockReport.lastMetricJsonReporter(filter);
    }


    public static InitializeContext getInitializeContext() {
        return EaseAgent.initializeContextSupplier.get();
    }

    public static Configs getConfigs() {
        return MockConfig.getCONFIGS();
    }

    public static void resetAll() {
        ContextUtils.resetAll();
    }

    public static void cleanPendingSpans() {
        MockTracingProvider.cleanPendingSpans();
    }

    public static void flushAllMetric() {
        MockMetricProvider.flush();
    }

    public static void cleanAllMetric() {
        MockMetricProvider.clearAll();
    }

    public static void cleanMetric(com.megaease.easeagent.plugin.api.metric.MetricRegistry metricRegistry) {
        MetricTestUtils.clear(metricRegistry);
    }

    public static void clearMetric(ServiceMetric serviceMetric) {
        MetricTestUtils.clear(serviceMetric);
    }
}
