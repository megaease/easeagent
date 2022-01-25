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

import com.megaease.easeagent.config.report.ReportConfigConst;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Callback;
import com.megaease.easeagent.plugin.report.Sender;

import java.io.IOException;
import java.util.Map;

public class ZipkinSender implements Sender {
    public static final String SENDER_NAME = ReportConfigConst.ZIPKIN_SENDER_NAME;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        // delay
    }

    @Override
    public Callback<Void> send(byte[] encodedData) {
        return null;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // delay
    }

    @Override
    public void close() throws IOException {
        // delay
    }
}
