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
package com.megaease.easeagent.plugin.report;

import com.megaease.easeagent.plugin.api.config.Config;

import java.io.Closeable;
import java.util.Map;

/**
 * borrow from OpenZipkin's Sender.
 */
public interface Sender extends Closeable {
    /**
     * Define an unique name for the sender
     */
    String name();

    /**
     * Initialize the sender with the configuration
     * @param config configuration with the prefix of "plugin.reporter.sender.[name]
     * @param prefix sender prefix : "reporter.tracing." is the prefix of "reporter.tracing.sender.[name]"
     */
    void init(Config config, String prefix);

    /**
     * Sends encoded data to a transport such as http or Kafka.
     *
     * @param encodedData encoded data, such as encoded spans.
     * @throws IllegalStateException if {@link #close() close} was called.
     */
    Call<Void> send(EncodedData encodedData);

    /**
     * If sender is available( not closed), return true, otherwise false.
     */
    boolean isAvailable();

    /**
     * when the configuration of sender changed, this method will be called
     * @param changes changed configuration KVs
     */
    void updateConfigs(Map<String, String> changes);
}
