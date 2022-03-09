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
package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.report.AgentReport;
import com.megaease.easeagent.plugin.report.metric.MetricReporterFactory;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;

import java.util.List;

public class NoOpAgentReporter implements AgentReport {
    @Override
    public void report(ReportSpan span) {
        // ignored
    }

    @Override
    public void report(AccessLogInfo log) {
        // ignored
    }

    @Override
    public MetricReporterFactory metricReporter() {
        return new MetricReporterFactory() {
            @Override
            public Reporter reporter(IPluginConfig config) {
                return NoOpReporter.NO_OP_REPORTER;
            }
        };
    }
}
