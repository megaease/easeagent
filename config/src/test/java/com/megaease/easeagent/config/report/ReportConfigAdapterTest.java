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
package com.megaease.easeagent.config.report;

import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.plugin.utils.NoNull;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class ReportConfigAdapterTest {
    @Test
    public void test_zipkin_target() {
        HashMap<String, String> cfg = new HashMap<>();
        String url = "http://localhost:9411/api/v2/spans";
        cfg.put("observability.tracings.output.target", "zipkin");
        cfg.put("observability.tracings.output.target.zipkinUrl", url);
        GlobalConfigs configs = new GlobalConfigs(cfg);

        String senderName = configs.getString(TRACE_SENDER_NAME);
        Assert.assertEquals(ZIPKIN_SENDER_NAME, senderName);
        Assert.assertEquals(url, configs.getString(join(TRACE_SENDER, "url")));
    }

    @Test
    public void test_console_target() {
        HashMap<String, String> cfg = new HashMap<>();
        String url = "";
        cfg.put("observability.tracings.output.target", "zipkin");
        cfg.put("observability.tracings.output.target.zipkinUrl", url);
        GlobalConfigs configs = new GlobalConfigs(cfg);

        Assert.assertEquals(CONSOLE_SENDER_NAME, configs.getString(TRACE_SENDER_NAME));

        cfg.clear();
        cfg.put("observability.tracings.output.target", "system");
        cfg.put("observability.tracings.output.topic", "log-tracing");
        configs = new GlobalConfigs(cfg);

        Assert.assertEquals(CONSOLE_SENDER_NAME, configs.getString(TRACE_SENDER_NAME));
    }

    @Test
    public void test_kafka_target() {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("observability.tracings.output.target", "system");
        cfg.put("observability.tracings.output.topic", "log-tracing");
        cfg.put("observability.outputServer.bootstrapServer", "127.0.0.1:9092");

        GlobalConfigs configs = new GlobalConfigs(cfg);

        Assert.assertEquals(KAFKA_SENDER_NAME, configs.getString(TRACE_SENDER_NAME));
    }

    @Test
    public void test_trace_output() {
        /*
         * observability.tracings.output.messageMaxBytes=999900
         * observability.tracings.output.reportThread=1
         * observability.tracings.output.queuedMaxSpans=1000
         * observability.tracings.output.queuedMaxSize=1000000
         * observability.tracings.output.messageTimeout=1000
         */
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("observability.tracings.output.messageMaxBytes", "123");
        cfg.put("observability.tracings.output.reportThread", "2");

        GlobalConfigs configs = new GlobalConfigs(cfg);
        long m = configs.getLong(TRACE_ASYNC_MESSAGE_MAX_BYTES_V2);
        Assert.assertEquals(123L, m);
        m = configs.getLong(TRACE_ASYNC_REPORT_THREAD_V2);
        Assert.assertEquals(2L, m);

        // override by v2
        cfg.clear();
        cfg.put("observability.tracings.output.queuedMaxSpans", "123");
        cfg.put(TRACE_ASYNC_QUEUED_MAX_SPANS_V2, "1000");

        configs = new GlobalConfigs(cfg);
        m = configs.getLong(TRACE_ASYNC_QUEUED_MAX_SPANS_V2);
        Assert.assertEquals(1000L, m);
    }

    @Test
    public void test_metric_global_v1() {
        /*
         * plugin.observability.global.metric.interval=30
         * plugin.observability.global.metric.topic=application-meter
         * plugin.observability.global.metric.appendType=kafka
         */
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("plugin.observability.global.metric.interval", "30");
        cfg.put("plugin.observability.global.metric.topic", "application-meter");
        cfg.put("plugin.observability.global.metric.appendType", "kafka");

        GlobalConfigs configs = new GlobalConfigs(cfg);
        Assert.assertEquals(30L, (long)NoNull.of(configs.getLong(METRIC_ASYNC_INTERVAL), 0L));
        Assert.assertEquals("application-meter", configs.getString(METRIC_SENDER_TOPIC));
        Assert.assertEquals(METRIC_KAFKA_SENDER_NAME, configs.getString(METRIC_SENDER_NAME));
    }
}
