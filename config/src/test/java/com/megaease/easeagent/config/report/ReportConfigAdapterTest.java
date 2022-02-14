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
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class ReportConfigAdapterTest {
    @Test
    public void test_zipkin_target() throws Exception {
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
    public void test_console_target() throws Exception {
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
    public void test_kafka_target() throws Exception {
        HashMap<String, String> cfg = new HashMap<>();
        cfg.put("observability.tracings.output.target", "system");
        cfg.put("observability.tracings.output.topic", "log-tracing");
        cfg.put("observability.outputServer.bootstrapServer", "127.0.0.1:9092");

        GlobalConfigs configs = new GlobalConfigs(cfg);

        Assert.assertEquals(KAFKA_SENDER_NAME, configs.getString(TRACE_SENDER_NAME));
    }
}
