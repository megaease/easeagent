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

package com.megaease.easeagent.report.sender.metric.log4j;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.utils.UtilsTest;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.NullAppender;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.Property;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class AppenderManagerTest {

    @Test
    public void create() throws IOException {
        Configs configs = UtilsTest.readConfigs("sender/outputServer_disabled.json");
        OutputProperties outputProperties = OutputProperties.newDefault(configs);
        AppenderManager appenderManager = AppenderManager.create(outputProperties);

        {
            Appender appender = appenderManager.appender("testTopic");
            assertTrue(appender instanceof NullAppender);
        }

        outputProperties.updateConfig(UtilsTest.readMap("sender/outputServer_empty_bootstrapServer_enabled.json"));
        appenderManager.refresh();

        {
            Appender appender = appenderManager.appender("testTopic");
            assertNull(appender);
        }

        outputProperties.updateConfig(UtilsTest.readMap("sender/outputServer_enabled.json"));
        appenderManager.refresh();

        {
            MockedStatic<KafkaAppender> kafkaAppenderMockedStatic = mockStatic(KafkaAppender.class);
            KafkaAppender.Builder builder = mock(KafkaAppender.Builder.class);
            when(KafkaAppender.newBuilder()).thenReturn(builder);
            when(builder.setTopic(anyString())).thenReturn(builder);
            when(builder.setSyncSend(anyBoolean())).thenReturn(builder);
            when(builder.setName(anyString())).thenReturn(builder);
            when(builder.setPropertyArray(any(Property[].class))).thenReturn(builder);
            when(builder.setLayout(any(Layout.class))).thenReturn(builder);
            when(builder.setConfiguration(any(Configuration.class))).thenReturn(builder);
            KafkaAppender mockAppender = mock(KafkaAppender.class);
            when(builder.build()).thenReturn(mockAppender);

            Appender appender = appenderManager.appender("testTopic");
            assertSame(mockAppender, appender);
            kafkaAppenderMockedStatic.close();
        }

    }
}
