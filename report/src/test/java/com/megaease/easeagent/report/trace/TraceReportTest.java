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

package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.config.report.ReportConfigAdapter;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.report.plugin.ReporterLoader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TraceReportTest {
    @Before
    public void before() {
        ReporterLoader.load();
    }

    @Test
    public void test1() throws InterruptedException {
        final HashMap<String, String> source = new HashMap<>();
        source.put(ConfigConst.SERVICE_NAME, "test-service");
        source.put(ConfigConst.Observability.TRACE_ENABLED, "true");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_ENABLED, "false");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_TOPIC, "log-tracing");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_REPORT_THREAD, "1");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_MESSAGE_TIMEOUT, "1000");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_MESSAGE_MAX_BYTES, "999900");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_QUEUED_MAX_SIZE, "1000000");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_QUEUED_MAX_SPANS, "1000");
        Map<String, String> cfg = ReportConfigAdapter.extractReporterConfig(new Configs(source));
        final TraceReport report = new TraceReport(new Configs(cfg));
        final ReportSpan build = ReportSpanBuilder.newBuilder()
                .traceId("122332")
                .id(1L)
                .timestamp(10000)
                .build();
        report.report(build);
        TimeUnit.SECONDS.sleep(3);
        Assert.assertTrue(true);
    }

    @Test
    public void test2() throws InterruptedException {
        final HashMap<String, String> source = new HashMap<>();
        source.put(ConfigConst.SERVICE_NAME, "test-service");
        source.put(ConfigConst.Observability.OUTPUT_SERVERS, "127.0.0.1:9093");
        source.put(ConfigConst.Observability.OUTPUT_TIMEOUT, "10000");
        source.put(ConfigConst.Observability.TRACE_ENABLED, "true");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_ENABLED, "true");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_TOPIC, "log-tracing");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_REPORT_THREAD, "1");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_MESSAGE_TIMEOUT, "1000");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_MESSAGE_MAX_BYTES, "999900");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_QUEUED_MAX_SIZE, "1000000");
        source.put(ConfigConst.Observability.TRACE_OUTPUT_QUEUED_MAX_SPANS, "1000");
        Map<String, String> cfg = ReportConfigAdapter.extractReporterConfig(new Configs(source));
        final TraceReport report = new TraceReport(new Configs(cfg));
        final ReportSpan build = ReportSpanBuilder.newBuilder()
                .traceId("122332")
                .id(1L)
                .timestamp(10000)
                .build();
        report.report(build);
        TimeUnit.SECONDS.sleep(3);
        Assert.assertTrue(true);
    }
}
