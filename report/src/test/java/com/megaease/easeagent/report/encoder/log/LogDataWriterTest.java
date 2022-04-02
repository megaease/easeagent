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
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LogDataWriterTest {
    Logger log = LoggerFactory.getLogger(LogDataWriterTest.class);
    Configs config;
    @Before
    public void init() {
        Map<String, String> cfg = new HashMap<>();
        cfg.put("timestamp", "%d{UNIX_MILLIS}");
        cfg.put("logLevel", "%-5level");
        cfg.put("threadId", "%thread");
        cfg.put("location", "%logger{36}");
        cfg.put("message", "%msg");
        this.config = new Configs(cfg);
    }

    @Test
    public void test_location_pattern() {
        LogDataWriter writer = new LogDataWriter(config);
    }
}
