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
package com.megaease.easeagent.report.sender;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Sender;

import java.io.IOException;
import java.util.Map;

/**
 * Send span data to agent log
 * It will be printed to console, when the logger configuration to append to console
 */
@AutoService(Sender.class)
public class AgentLoggerSender implements Sender {
    public static final String SENDER_NAME = ReportConfigConst.CONSOLE_SENDER_NAME;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentLoggerSender.class);
    private String prefix;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config, String prefix) {
        // ignored
        this.prefix = prefix;
    }

    @Override
    public Call<Void> send(EncodedData encodedData) {
        return new ConsoleCall(encodedData.getData());
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // ignored
    }

    @Override
    public void close() throws IOException {
        // ignored
    }

    static class ConsoleCall implements Call<Void> {
        private final byte[] msg;

        ConsoleCall(byte[] msg) {
            this.msg = msg;
        }

        @Override
        public Void execute() throws IOException {
            LOGGER.info("{}", new String(msg));
            return null;
        }

        @Override
        public void enqueue(Callback<Void> cb) {
            LOGGER.debug("{}", new String(msg));
        }
    }
}
