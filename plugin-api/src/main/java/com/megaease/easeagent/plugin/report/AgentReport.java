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

package com.megaease.easeagent.plugin.report;

import com.megaease.easeagent.plugin.api.logging.AccessLogInfo;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.report.metric.MetricReporterFactory;

/**
 * report interface:
 * trace/metric/accessLog
 */
public interface AgentReport {
    /**
     * report trace span
     * @param span trace span
     */
    void report(ReportSpan span);

    /**
     * report access-log
     * @param log log info
     */
    void report(AccessLogInfo log);

    /**
     * report application log
     * @param log log info
     */
    void report(AgentLogData log);

    /**
     * Metric reporters factory
     * @return metric reporters factory
     */
    MetricReporterFactory metricReporter();
}
