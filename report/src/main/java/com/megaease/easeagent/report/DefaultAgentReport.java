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
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.config.report.ReportConfigAdapter;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.report.metric.MetricReporter;
import com.megaease.easeagent.report.metric.MetricReporterImpl;
import com.megaease.easeagent.report.plugin.ReporterLoader;
import com.megaease.easeagent.report.trace.TraceReport;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

@Slf4j
public class DefaultAgentReport implements AgentReport, ConfigChangeListener {
    private final TraceReport traceReport;
    private final MetricReporter metricReporter;
    private final Config reportConfig;

    DefaultAgentReport(Config config) {
        this.reportConfig = new Configs(ReportConfigAdapter.extractReporterConfig(config));
        this.traceReport = new TraceReport(this.reportConfig);
        this.metricReporter = MetricReporterImpl.create(this.reportConfig);
    }

    public static AgentReport create(Configs config) {
        ReporterLoader.load();
        return new DefaultAgentReport(config);
    }

    @Override
    public void report(ReportSpan span) {
        if (log.isDebugEnabled()) {
            log.debug("traceId: {}, spanId: {}", span.traceId(), span.id());
        }
        this.traceReport.report(span);
    }

    @Override
    public MetricReporter metricReporter() {
        return this.metricReporter;
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        Map<String, String> changes = filterChanges(list);
        if (changes.isEmpty()) {
            return;
        }
        Config global = EaseAgent.getConfig();
        Map<String, String> reportCfg = ReportConfigAdapter.extractReporterConfig(global);
        this.reportConfig.updateConfigs(reportCfg);
    }

    private Map<String, String> filterChanges(List<ChangeItem> list) {
        Map<String, String> cfg = new HashMap<>();
        list.stream()
            .filter(one -> {
                String name = one.getFullName();
                return name.startsWith(OUTPUT_SERVER_V1)
                    || name.startsWith(TRACE_OUTPUT_V1)
                    || name.startsWith(GLOBAL_METRIC)
                    || name.startsWith(REPORT);
            }).forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));

        return cfg;
    }
}
