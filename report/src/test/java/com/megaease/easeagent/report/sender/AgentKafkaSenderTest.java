/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.report.sender;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.report.plugin.NoOpCall;
import com.megaease.easeagent.report.utils.UtilsTest;
import org.junit.After;
import org.junit.Test;
import org.mockito.MockedStatic;
import zipkin2.codec.Encoding;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.kafka11.SDKKafkaSender;

import java.io.IOException;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.KAFKA_SENDER_NAME;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AgentKafkaSenderTest {
    MockedStatic<KafkaSender> kafkaSenderMockedStatic;
    MockedStatic<SDKKafkaSender> sdkKafkaSenderMockedStatic;


    @After
    public void after() {
        if (kafkaSenderMockedStatic != null) {
            kafkaSenderMockedStatic.close();
            kafkaSenderMockedStatic = null;
        }
        if (sdkKafkaSenderMockedStatic != null) {
            sdkKafkaSenderMockedStatic.close();
            sdkKafkaSenderMockedStatic = null;
        }
    }

    @Test
    public void testName() throws IOException {
        AgentKafkaSender agentKafkaSender = new AgentKafkaSender();
        assertEquals(KAFKA_SENDER_NAME, agentKafkaSender.name());
    }

    @Test
    public void test_outputServer_disabled() throws IOException, NoSuchFieldException, IllegalAccessException {
        Configs configs = readConfigs("sender/outputServer_disabled.json");
        AgentKafkaSender agentKafkaSender = new AgentKafkaSender();
        agentKafkaSender.init(configs, "reporter.log.access.sender");
        SDKKafkaSender sender = UtilsTest.getFiled(agentKafkaSender, "sender");
        assertNull(sender);
        Call result = agentKafkaSender.send(null);
        assertTrue(result instanceof NoOpCall);
        assertFalse(agentKafkaSender.isAvailable());
        agentKafkaSender.close();
    }

    @Test
    public void test_outputServer_enabled() throws IOException, NoSuchFieldException, IllegalAccessException {
        Configs configs = readConfigs("sender/outputServer_enabled.json");
        AgentKafkaSender agentKafkaSender = new AgentKafkaSender();

        SDKKafkaSender sdkKafkaSender = mockKafkaSender();

        agentKafkaSender.init(configs, "reporter.log.access.sender");
        SDKKafkaSender sender = UtilsTest.getFiled(agentKafkaSender, "sender");
        assertNotNull(sender);
        assertSame(sdkKafkaSender, sender);
        Call result = agentKafkaSender.send(EncodedData.EMPTY);
        assertTrue(result instanceof ZipkinCallWrapper);
        assertTrue(agentKafkaSender.isAvailable());
        agentKafkaSender.close();
    }

    @Test
    public void test_outputServer_updateConfigs() throws IOException, NoSuchFieldException, IllegalAccessException {
        Configs configs = readConfigs("sender/outputServer_disabled.json");
        AgentKafkaSender agentKafkaSender = new AgentKafkaSender();
        agentKafkaSender.init(configs, "reporter.log.access.sender");
        assertFalse(agentKafkaSender.isAvailable());

        String config = UtilsTest.readFromResourcePath("sender/outputServer_enabled.json");
        Map<String, String> map = (Map<String, String>) (Map) JsonUtil.toMap(config);
        SDKKafkaSender sdkKafkaSender = mockKafkaSender();
        agentKafkaSender.updateConfigs(map);
        SDKKafkaSender sender = UtilsTest.getFiled(agentKafkaSender, "sender");
        assertNotNull(sender);
        assertSame(sdkKafkaSender, sender);
    }

    @Test
    public void test_outputServer_empty_bootstrapServer_updateConfigs() throws IOException, NoSuchFieldException, IllegalAccessException {
        Configs configs = readConfigs("sender/outputServer_empty_bootstrapServer_disabled.json");
        AgentKafkaSender agentKafkaSender = new AgentKafkaSender();
        agentKafkaSender.init(configs, "reporter.log.access.sender");
        assertFalse(agentKafkaSender.isAvailable());

        {
            String config = UtilsTest.readFromResourcePath("sender/outputServer_empty_bootstrapServer_enabled.json");
            Map<String, String> map = (Map<String, String>) (Map) JsonUtil.toMap(config);
            agentKafkaSender.updateConfigs(map);
            SDKKafkaSender sender = UtilsTest.getFiled(agentKafkaSender, "sender");
            assertNull(sender);
        }

        {
            String config = UtilsTest.readFromResourcePath("sender/outputServer_enabled.json");
            Map<String, String> map = (Map<String, String>) (Map) JsonUtil.toMap(config);
            SDKKafkaSender sdkKafkaSender = mockKafkaSender();
            agentKafkaSender.updateConfigs(map);
            SDKKafkaSender sender = UtilsTest.getFiled(agentKafkaSender, "sender");
            assertNotNull(sender);
            assertSame(sdkKafkaSender, sender);
        }

    }

    private SDKKafkaSender mockKafkaSender() {
        KafkaSender.Builder builder = mock(KafkaSender.Builder.class);
        KafkaSender kafkaSender = mock(KafkaSender.class);
        kafkaSenderMockedStatic = mockStatic(KafkaSender.class);
        when(KafkaSender.newBuilder()).thenReturn(builder);
        when(builder.build()).thenReturn(kafkaSender);
        when(builder.bootstrapServers(anyString())).thenReturn(builder);
        when(builder.topic(anyString())).thenReturn(builder);
        when(builder.overrides(anyMap())).thenReturn(builder);
        when(builder.encoding(Encoding.JSON)).thenReturn(builder);
        when(builder.messageMaxBytes(anyInt())).thenReturn(builder);
        when(builder.build()).thenReturn(kafkaSender);

        SDKKafkaSender sdkKafkaSender = mock(SDKKafkaSender.class);
        sdkKafkaSenderMockedStatic = mockStatic(SDKKafkaSender.class);
        when(SDKKafkaSender.wrap(kafkaSender)).thenReturn(sdkKafkaSender);
        return sdkKafkaSender;
    }

    public Configs readConfigs(String path) throws IOException {
        String config = UtilsTest.readFromResourcePath(path);
        Map<String, String> map = (Map<String, String>) (Map) JsonUtil.toMap(config);
        return new Configs(map);
    }


}
