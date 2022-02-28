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

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.report.plugin.NoOpCall;

import java.io.IOException;
import java.util.Map;

public class NoOpSender implements Sender {
    public static final NoOpSender INSTANCE = new NoOpSender();

    @Override
    public String name() {
        return "noop";
    }

    @Override
    public void init(Config config, String prefix) {
        // ignored
    }

    @Override
    public Call<Void> send(EncodedData encodedData) {
        return new NoOpCall<>();
    }

    @Override
    public boolean isAvailable() {
        return false;
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
