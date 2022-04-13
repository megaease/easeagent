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

import com.google.common.base.CharMatcher;
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
                .thread(Thread.currentThread())
                .throwable(new NullPointerException("test"))
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
        String original = this.config.getString(join(ENCODER_KEY, "location"));
        changes.put(join(ENCODER_KEY, "location"), "%logger{2}");
        this.config.updateConfigs(changes);

        int size = encoder.sizeInBytes(data);
        Assert.assertEquals(195, size);
        EncodedData encoded = encoder.encode(data);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));
        Assert.assertEquals("log.LogDataJsonEncoderTest", jsonMap.get(LOCATION));

        changes.put(join(ENCODER_KEY, "location"), original);
        this.config.updateConfigs(changes);
    }

    @Test
    public void test_throwable_encoder() {
        Map<String, String> changes = new HashMap<>();

        String key = join(ENCODER_KEY, "message");
        String original = this.config.getString(key);
        changes.put(key, "%msg%n%xEx");
        this.config.updateConfigs(changes);

        AgentLogData exceptionData = AgentLogDataImpl.builder()
            .epochMills(1648878722451L)
            .logger(log.getName())
            .severity(Severity.INFO)
            .severityText(Level.INFO.toString())
            .thread(Thread.currentThread())
            .throwable(new NullPointerException("test"))
            .body("Hello")
            .build();

        EncodedData encoded = encoder.encode(exceptionData);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));

        Assert.assertTrue(jsonMap.get("message").toString().contains(NullPointerException.class.getCanonicalName()));

        changes.put(key, original);
        this.config.updateConfigs(changes);
    }

    @Test
    public void test_throwable_short_encoder() {
        Map<String, String> changes = new HashMap<>();

        String key = join(ENCODER_KEY, "message");
        String original = this.config.getString(key);
        changes.put(key, "%msg%xEx{5,separator(|)}");
        this.config.updateConfigs(changes);

        AgentLogData exceptionData = AgentLogDataImpl.builder()
            .epochMills(1648878722451L)
            .logger(log.getName())
            .severity(Severity.INFO)
            .severityText(Level.INFO.toString())
            .thread(Thread.currentThread())
            .throwable(new NullPointerException("test"))
            .body("Hello")
            .build();

        // int size = encoder.sizeInBytes(exceptionData);
        // Assert.assertEquals(668, size);
        EncodedData encoded = encoder.encode(exceptionData);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));

        String message = jsonMap.get("message").toString();
        Assert.assertTrue(message.contains(NullPointerException.class.getCanonicalName()));
        int count = CharMatcher.is('|').countIn(message);
        Assert.assertEquals(4, count);

        changes.put(key, original);
        this.config.updateConfigs(changes);
    }

    @Test
    public void test_mdc_selected() {
        Map<String, String> changes = new HashMap<>();

        String key = join(ENCODER_KEY, "message");
        String original = this.config.getString(key);
        changes.put(key, "%X{name, number}-%msg%xEx{5,separator(|)}");
        this.config.updateConfigs(changes);

        AgentLogDataImpl.Builder builder = AgentLogDataImpl.builder()
            .epochMills(1648878722451L)
            .logger(log.getName())
            .severity(Severity.INFO)
            .severityText(Level.INFO.toString())
            .thread(Thread.currentThread())
            .body("Hello");

        AgentLogData noMdcData = builder.build();
        int size = encoder.sizeInBytes(noMdcData);
        Assert.assertEquals(206, size);
        EncodedData encoded = encoder.encode(noMdcData);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));
        String message = jsonMap.get("message").toString();
        Assert.assertTrue(message.startsWith("{}-Hello"));

        // test mdc
        Map<String, String> ctxData = new HashMap<>();
        ctxData.put("name", "easeagent");
        ctxData.put("number", "v2.2");
        builder.contextData(null, ctxData);
        AgentLogData mdcData = builder.build();

        size = encoder.sizeInBytes(mdcData);
        Assert.assertEquals(233, size);
        encoded = encoder.encode(mdcData);
        jsonMap = JsonUtil.toMap(new String(encoded.getData()));
        message = jsonMap.get("message").toString();
        Assert.assertTrue(message.startsWith("{name=easeagent, number=v2.2}-Hello"));

        changes.put(key, original);
        this.config.updateConfigs(changes);
    }

    @Test
    public void test_custom() {
        Map<String, String> original = this.config.getConfigs();
        Map<String, String> changes = new HashMap<>();
        changes.put("encoder.custom", "%X{custom}");

        this.config.updateConfigs(changes);

        AgentLogDataImpl.Builder builder = AgentLogDataImpl.builder()
            .epochMills(1648878722451L)
            .logger(log.getName())
            .severity(Severity.INFO)
            .severityText(Level.INFO.toString())
            .thread(Thread.currentThread())
            .body("Hello");

        // test mdc
        Map<String, String> ctxData = new HashMap<>();
        ctxData.put("custom", "easeagent");
        builder.contextData(null, ctxData);
        AgentLogData mdcData = builder.build();

        int size = encoder.sizeInBytes(mdcData);
        Assert.assertEquals(224, size);
        EncodedData encoded = encoder.encode(mdcData);
        Map<String, Object> jsonMap = JsonUtil.toMap(new String(encoded.getData()));
        String custom = jsonMap.get("custom").toString();
        Assert.assertEquals("easeagent", custom);

        this.config = new Configs(original);
    }
}
