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

package com.megaease.easeagent.mock.report;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.DefaultAgentReport;
import com.megaease.easeagent.report.metric.MetricReporter;
import com.megaease.easeagent.report.util.SpanUtils;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class MockReport {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockReport.class);
    private static final AgentReport AGENT_REPORT = new MockAgentReport(DefaultAgentReport.create(MockConfig.getCONFIGS()));

    private static final AtomicReference<ReportSpan> LAST_SPAN = new AtomicReference<>();
    private static final AtomicReference<ReportSpan> LAST_SKIP_SPAN = new AtomicReference<>();
    private static volatile MetricFlushable metricFlushable;
    private static volatile MockSpanReport mockSpanReport = null;
    private static volatile Reporter metricReportMock = null;
    private static volatile JsonReporter metricJsonReport = null;

    public static AgentReport getAgentReport() {
        return AGENT_REPORT;
    }

    public static void setMetricFlushable(MetricFlushable metricFlushable) {
        MockReport.metricFlushable = metricFlushable;
    }

    public static void setMockSpanReport(MockSpanReport mockSpanReport) {
        MockReport.mockSpanReport = mockSpanReport;
    }

    public static void setMockMetricReport(Reporter metricReportMock) {
        MockReport.metricReportMock = metricReportMock;
    }

    public static LastJsonReporter lastMetricJsonReporter(Predicate<Map<String, Object>> filter) {
        LastJsonReporter lastJsonReporter = new LastJsonReporter(filter, metricFlushable);
        metricJsonReport = lastJsonReporter;
        return lastJsonReporter;
    }

    public static void cleanReporter() {
        mockSpanReport = null;
        metricReportMock = null;
        metricJsonReport = null;
    }


    private static void reportMetricToJson(String text) {
        if (metricJsonReport == null) {
            return;
        }
        try {
            List<Map<String, Object>> json;
            if (text.trim().startsWith("{")) {
                Map<String, Object> jsonMap = JsonUtil.toMap(text);
                json = Collections.singletonList(jsonMap);
            } else {
                json = JsonUtil.toList(text);
            }
            metricJsonReport.report(json);
        } catch (Exception e) {
            LOGGER.error("string to List<Map<String, Object>> fail: {}", e.getMessage());
        }
    }

    static class MockAgentReport implements AgentReport {
        private final AgentReport agentReport;
        private final MockMetricReporter pluginMetricReporter;

        MockAgentReport(AgentReport agentReport) {
            this.agentReport = agentReport;
            this.pluginMetricReporter = new MockMetricReporter(agentReport.metricReporter());
        }

        @Override
        public void report(ReportSpan span) {
            agentReport.report(span);
            if (!SpanUtils.isValidSpan(span)) {
                LOGGER.warn("span<traceId({}), id({}), name({}), kind({})> not start(), skip it.", span.traceId(), span.id(), span.name(), span.kind());
                LAST_SKIP_SPAN.set(span);
                return;
            }
            if (span.duration() == 0) {
                LOGGER.warn(String.format("span<traceId(%s), id(%s), name(%s), kind(%s), timestamp(%s) duration(%s) not finish, skip it.", span.traceId(), span.id(), span.name(), span.kind(), span.timestamp(), span.duration()));
                LAST_SKIP_SPAN.set(span);
                return;
            }
            // MockSpan mockSpan = new ZipkinMockSpanImpl(span);
            LAST_SPAN.set(span);
            try {
                MockSpanReport mockSpanReport = MockReport.mockSpanReport;
                if (mockSpanReport != null) {
                    mockSpanReport.report(span);
                }
            } catch (Exception e) {
                LOGGER.error("mock span report : {}", e);
            }
        }

        @Override
        public MetricReporter metricReporter() {
            return pluginMetricReporter;
        }
    }

    static class MockMetricReporter implements MetricReporter {
        private final MetricReporter metricReporter;

        MockMetricReporter(@Nonnull MetricReporter metricReporter) {
            this.metricReporter = metricReporter;
        }

        @Override
        public Reporter reporter(IPluginConfig config) {
            return new MockReporter(metricReporter.reporter(config));
        }

        @Override
        public void onChange(List<ChangeItem> list) {
            //ignored
        }
    }

    static class MockReporter implements Reporter {
        private final Reporter reporter;

        MockReporter(@Nonnull Reporter reporter) {
            this.reporter = reporter;
        }

        @Override
        public void report(String msg) {
            reporter.report(msg);
            try {
                Reporter reportMock = metricReportMock;
                if (reportMock != null) {
                    reportMock.report(msg);
                }
            } catch (Exception e) {
                LOGGER.error("mock metric report fail: {}", e);
            }
            reportMetricToJson(msg);
        }

        @Override
        public void report(EncodedData msg) {
            this.report(new String(msg.getData()));
        }
    }

    public static ReportSpan getLastSpan() {
        return LAST_SPAN.get();
    }

    public static void cleanLastSpan() {
        LAST_SPAN.set(null);
    }

    public static ReportSpan getLastSkipSpan() {
        return LAST_SKIP_SPAN.get();
    }

    public static void cleanSkipSpan() {
        LAST_SKIP_SPAN.set(null);
    }
}
