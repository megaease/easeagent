/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.metric.PluginMetricReporterImpl;
import com.megaease.easeagent.report.trace.TraceReport;
import zipkin2.Span;

public interface AgentReport {
    void report(Span span);

    PluginMetricReporter pluginMetricReporter();

    static AgentReport create(Configs config) {
        return new Default(new TraceReport(config), PluginMetricReporterImpl.create(config));
    }

    class Default implements AgentReport {
        private TraceReport traceReport;
        private PluginMetricReporter pluginMetricReporter;

        public Default(TraceReport traceReport, PluginMetricReporter pluginMetricReporter) {
            this.traceReport = traceReport;
            this.pluginMetricReporter = pluginMetricReporter;
        }

        @Override
        public void report(Span span) {
            this.traceReport.report(span);
        }

        @Override
        public PluginMetricReporter pluginMetricReporter() {
            return this.pluginMetricReporter;
        }
    }
}
