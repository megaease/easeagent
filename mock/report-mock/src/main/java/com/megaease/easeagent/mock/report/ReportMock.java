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
import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.PluginMetricReporter;
import com.megaease.easeagent.report.util.SpanUtils;
import zipkin2.Span;

import javax.annotation.Nonnull;

public class ReportMock {
    public static final Logger LOGGER = LoggerFactory.getLogger(ReportMock.class);
    public static final AgentReport AGENT_REPORT = new MockAgentReport(AgentReport.create(ConfigMock.getCONFIGS()));
    public static volatile SpanReportMock SPAN_REPORT_MOCK = null;
    public static volatile Reporter METRIC_REPORT_MOCK = null;

    public static AgentReport getAgentReport() {
        return AGENT_REPORT;
    }

    public static void setSpanReportMock(SpanReportMock spanReportMock) {
        SPAN_REPORT_MOCK = spanReportMock;
    }

    public static void setMetricReportMock(Reporter metricReportMock) {
        METRIC_REPORT_MOCK = metricReportMock;
    }

    static class MockAgentReport implements AgentReport {
        private final AgentReport agentReport;
        private final MockPluginMetricReporter pluginMetricReporter;

        MockAgentReport(AgentReport agentReport) {
            this.agentReport = agentReport;
            this.pluginMetricReporter = new MockPluginMetricReporter(agentReport.pluginMetricReporter());
        }

        @Override
        public void report(Span span) {
            if (!SpanUtils.isValidSpan(span)) {
                LOGGER.error("span<traceId({}), id({}), name({}), kind({})> not start(), please call span.start() before span.finish().", span.traceId(), span.id(), span.name(), span.kind());
            }
            agentReport.report(span);
            try {
                SpanReportMock spanReportMock = SPAN_REPORT_MOCK;
                if (spanReportMock != null) {
                    spanReportMock.report(span);
                }
            } catch (Exception e) {
                LOGGER.error("mock span report : {}", e);
            }
        }

        @Override
        public PluginMetricReporter pluginMetricReporter() {
            return pluginMetricReporter;
        }
    }


    static class MockPluginMetricReporter implements PluginMetricReporter {
        private final PluginMetricReporter pluginMetricReporter;

        MockPluginMetricReporter(@Nonnull PluginMetricReporter pluginMetricReporter) {
            this.pluginMetricReporter = pluginMetricReporter;
        }

        @Override
        public Reporter reporter(Config config) {
            return new MockReporter(pluginMetricReporter.reporter(config));
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
                Reporter reporter = METRIC_REPORT_MOCK;
                if (reporter != null) {
                    reporter.report(msg);
                }
            } catch (Exception e) {
                LOGGER.error("mock metric report fail: {}", e);
            }
        }
    }
}
