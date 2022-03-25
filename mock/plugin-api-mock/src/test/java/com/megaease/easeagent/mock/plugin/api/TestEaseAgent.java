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

package com.megaease.easeagent.mock.plugin.api;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.EncodedData;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class TestEaseAgent {
    @Test
    public void testEaseAgent() {
        assertNotNull(EaseAgent.initializeContextSupplier);
        assertNotNull(EaseAgent.getContext());
        assertFalse(EaseAgent.getContext().isNoop());
        assertNotNull(EaseAgent.configFactory);
        assertNotNull(EaseAgent.configFactory.getConfig("name"));
        IPluginConfig config = EaseAgent.configFactory.getConfig("test1", "test2", "test3");
        assertNotNull(config);
        assertNotNull(EaseAgent.metricRegistrySupplier);
        assertNotNull(EaseAgent.metricRegistrySupplier.reporter(config));
        AtomicReference<String> message = new AtomicReference<>("");
        MockReport.setMockMetricReport(new Reporter() {
            @Override
            public void report(String msg) {
                message.set(msg);
            }

            @Override
            public void report(EncodedData msg) {
                message.set(new String(msg.getData()));
            }
        });
        EaseAgent.metricRegistrySupplier.reporter(config).report("test");
        assertEquals("test",message.get());
        Logger logger = EaseAgent.getLogger(TestEaseAgent.class);
        logger.info("-------------------- easeagent test");
    }
}
