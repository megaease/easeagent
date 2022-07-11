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
package com.megaease.easeagent.report.sender;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.report.plugin.NoOpCall;

import java.io.IOException;
import java.util.Map;

@AutoService(Sender.class)
public class NoOpSender implements Sender {
    public static final NoOpSender INSTANCE = new NoOpSender();

    @Override
    public String name() {
        return ReportConfigConst.NOOP_SENDER_NAME;
    }

    @Override
    public void init(Config config, String prefix) {
        // ignored
    }

    @Override
    public Call<Void> send(EncodedData encodedData) {
        return NoOpCall.getInstance(NoOpSender.class);
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
}
