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
import com.megaease.easeagent.config.report.ReportConfigAdapter;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.report.AgentReport;
import com.megaease.easeagent.plugin.report.metric.MetricReporterFactory;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.report.async.log.LogReporter;
import com.megaease.easeagent.report.metric.MetricReporterFactoryImpl;
import com.megaease.easeagent.report.plugin.ReporterLoader;
import com.megaease.easeagent.report.trace.TraceReport;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Slf4j
public class DefaultAgentReport implements AgentReport, ConfigChangeListener {
    private final TraceReport traceReport;
    private final MetricReporterFactory metricReporterFactory;
    private final LogReporter logReporter;
    private final Config config;
    private final Config reportConfig;

    DefaultAgentReport(Config config) {
        this.config = config;
        this.reportConfig = new Configs(ReportConfigAdapter.extractReporterConfig(config));
        this.traceReport = new TraceReport(this.reportConfig);
        this.logReporter = new LogReporter(this.reportConfig);
        this.metricReporterFactory = MetricReporterFactoryImpl.create(this.reportConfig);
        this.config.addChangeListener(this);
    }

    public static AgentReport create(Configs config) {
        ReporterLoader.load();
        return new DefaultAgentReport(config);
    }

    @Override
    public void report(ReportSpan span) {
        this.traceReport.report(span);
    }

    @Override
    public void report(AccessLogInfo log) {
        this.logReporter.report(log);
    }

    @Override
    public MetricReporterFactory metricReporter() {
        return this.metricReporterFactory;
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        Map<String, String> changes = ReportConfigAdapter.extractReporterConfig(this.config);
        this.reportConfig.updateConfigs(changes);
    }
}
