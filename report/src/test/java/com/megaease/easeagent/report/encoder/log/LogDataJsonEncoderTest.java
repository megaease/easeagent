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
package com.megaease.easeagent.report.encoder.log;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogDataImpl;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import io.opentelemetry.sdk.logs.data.Severity;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.ENCODER_KEY;
import static com.megaease.easeagent.config.report.ReportConfigConst.join;
import static com.megaease.easeagent.report.encoder.log.LogDataWriter.LOCATION;
import static com.megaease.easeagent.report.encoder.log.LogDataWriter.LOG_LEVEL;

public class LogDataJsonEncoderTest {
    Logger log = LoggerFactory.getLogger(LogDataJsonEncoderTest.class);
    LogDataJsonEncoder encoder;
    Configs config;

    AgentLogData data;

    @Before
    public void init() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put(ENCODER_KEY, LogDataJsonEncoder.ENCODER_NAME);
        cfg.put(join(ENCODER_KEY, "timestamp"), "%d{UNIX_MILLIS}");
        cfg.put(join(ENCODER_KEY, "logLevel"), "%-5level");
        cfg.put(join(ENCODER_KEY, "threadId"), "%thread");
        cfg.put(join(ENCODER_KEY, "location"), "%logger{3}");
        cfg.put(join(ENCODER_KEY, "message"), "%msg");
        cfg.put("name", "demo-service");
        cfg.put("system", "demo-system");
        this.config = new Configs(cfg);

        this.data =
            AgentLogDataImpl.builder()
                .epochMills(1648878722451L)
                .logger(log.getName())
                .severity(Severity.INFO)
                .severityText(Level.INFO.toString())
                .threadName(Thread.currentThread().getName())
                .body("Hello")
                .build();

        encoder = new LogDataJsonEncoder();
        encoder.init(this.config);
    }

    @Test
    public void test_encoder() {
        // size = 208
        int size = encoder.sizeInBytes(data);
        Assert.assertEquals(203, size);
        EncodedData encoded = encoder.encode(data);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));
        Assert.assertEquals("encoder.log.LogDataJsonEncoderTest", jsonMap.get(LOCATION));
        Assert.assertEquals(5, jsonMap.get(LOG_LEVEL).toString().length());
    }

    @Test
    public void test_encoder_update() {
        Map<String, String> changes = new HashMap<>();
        changes.put(join(ENCODER_KEY, "location"), "%logger{2}");
        this.config.updateConfigs(changes);

        int size = encoder.sizeInBytes(data);
        Assert.assertEquals(195, size);
        EncodedData encoded = encoder.encode(data);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));
        Assert.assertEquals("log.LogDataJsonEncoderTest", jsonMap.get(LOCATION));
    }
}
