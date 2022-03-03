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

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.report.*;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.megaease.easeagent.config.ConfigUtils.extractByPrefix;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;

@Slf4j
public class SenderConfigDecorator
    implements SenderWithEncoder, ConfigChangeListener {

    protected Sender sender;
    String prefix;
    Config config;
    String encoderKey;
    Encoder<?> packer;

    public SenderConfigDecorator(String prefix, Sender sender, Config config) {
        this.sender = sender;
        this.prefix = prefix;
        this.encoderKey = getEncoderKey(prefix);
        config.addChangeListener(this);
        this.config = new Configs(extractSenderConfig(this.prefix, config));
    }

    @Override
    public String name() {
        return sender.name();
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public void init(Config config, String prefix) {
        this.packer = ReporterRegistry.getEncoder(config.getString(this.encoderKey));
        this.packer.init(this.config);
        this.sender.init(this.config, prefix);
    }

    @Override
    public Call<Void> send(EncodedData encodedData) {
        return this.sender.send(encodedData);
    }

    @Override
    public Call<Void> send(List<EncodedData> encodedData) {
        EncodedData data = this.packer.encodeList(encodedData);
        if (log.isDebugEnabled()) {
            log.debug("Span:{}", new String(data.getData()));
        }
        return send(data);
    }

    @Override
    public boolean isAvailable() {
        return sender.isAvailable();
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        String name = changes.get(join(this.prefix, NAME_KEY));
        if (name == null || name.equals(name())) {
            this.sender.updateConfigs(changes);
        } else {
            try {
                this.sender.close();
            } catch (IOException e) {
                log.warn("Sender update fail, can not close sender:{}", this.sender.name());
            }
        }
        updateEncoder(changes);
    }

    // checkEncoder update
    protected void updateEncoder(Map<String, String> changes) {
        String name = changes.get(this.encoderKey);
        if (name == null || name.equals(this.packer.name())) {
            return;
        }
        this.packer = ReporterRegistry.getEncoder(config.getString(this.encoderKey));
        this.packer.init(config);
    }

    @Override
    public void close() throws IOException {
        sender.close();
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        Map<String, String> changes = filterChanges(list);
        if (changes.isEmpty()) {
            return;
        }
        this.config.updateConfigs(changes);
        this.updateConfigs(changes);
    }

    private static String getEncoderKey(String cfgPrefix) {
        if (cfgPrefix.startsWith(TRACE_SENDER)) {
            return TRACE_ENCODER;
        } else {
            return METRIC_ENCODER;
        }
    }

    private static Map<String, String> extractSenderConfig(String cfgPrefix, Config config) {
        Map<String, String> extract = extractByPrefix(config, cfgPrefix);
        Map<String, String> cfg = new HashMap<>(extract);

        // outputServer config
        cfg.putAll(extractByPrefix(config, OUTPUT_SERVER_V2));

        return cfg;
    }

    private Map<String, String> filterChanges(List<ChangeItem> list) {
        Map<String, String> cfg = new HashMap<>();
        list.stream()
            .filter(one -> {
                String name = one.getFullName();
                return name.startsWith(prefix)
                    || name.startsWith(this.encoderKey)
                    || name.startsWith(OUTPUT_SERVER_V2);
            }).forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));

        return cfg;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Encoder<T> getEncoder() {
        return (Encoder<T>)this.packer;
    }
}
