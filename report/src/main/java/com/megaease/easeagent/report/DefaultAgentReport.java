/*
 * Copyright (c) 2021, MegaEase
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
package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.metric.MetricReporter;
import com.megaease.easeagent.report.metric.MetricReporterImpl;
import com.megaease.easeagent.report.trace.TraceReport;
import zipkin2.Span;

public class DefaultAgentReport implements AgentReport {
    private TraceReport traceReport;
    private MetricReporter metricReporter;

    public DefaultAgentReport(TraceReport traceReport, MetricReporter metricReporter) {
        this.traceReport = traceReport;
        this.metricReporter = metricReporter;
    }

    public static AgentReport create(Configs config) {
        return new DefaultAgentReport(new TraceReport(config), MetricReporterImpl.create(config));
    }

    @Override
    public void report(Span span) {
        this.traceReport.report(span);
    }

    @Override
    public MetricReporter metricReporter() {
        return this.metricReporter;
    }
}
