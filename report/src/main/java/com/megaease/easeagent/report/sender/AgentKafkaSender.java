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
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.report.Call;
import com.megaease.easeagent.plugin.report.Sender;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.plugin.NoOpCall;
import zipkin2.codec.Encoding;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.kafka11.SDKKafkaSender;

import java.io.IOException;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

@AutoService(Sender.class)
public class AgentKafkaSender implements Sender {
    public static final String SENDER_NAME = KAFKA_SENDER_NAME;
    private boolean enabled;
    private Config config;

    SDKKafkaSender sender;
    Map<String, String> ssl;

    @Override
    public String name() {
        return SENDER_NAME;
    }

    @Override
    public void init(Config config) {
        this.config = config;
        String outputServer = config.getString(BOOTSTRAP_SERVERS);
        if (StringUtils.isEmpty(outputServer)) {
            this.enabled = false;
            return;
        } else {
            enabled = checkEnable(config);
        }

        String topic = config.getString(TRACE_SENDER_TOPIC_V2);
        int msgMaxBytes = config.getInt(TRACE_ASYNC_MESSAGE_MAX_BYTES_V2);
        this.ssl = ConfigUtils.extractByPrefix(config, OUTPUT_SERVERS_SSL);

        this.sender = SDKKafkaSender.wrap(KafkaSender.newBuilder()
            .bootstrapServers(outputServer)
            .topic(topic)
            .overrides(ssl)
            .encoding(Encoding.JSON)
            .messageMaxBytes(msgMaxBytes)
            .build());
    }

    @Override
    public Call<Void> send(byte[] encodedData) {
        if (!enabled) {
            return new NoOpCall<>();
        }
        zipkin2.Call<Void> call =  this.sender.sendSpans(encodedData);
        return new ZipkinCallWrapper<>(call);
    }

    @Override
    public boolean isAvailable() {
        return (this.sender != null) && (!this.sender.isClose());
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        if (!SENDER_NAME.equals(changes.get(TRACE_SENDER_NAME))) {
            try {
                this.close();
            } catch (IOException e) {
                // ignored
            }
        }
        boolean refresh = false;
        for (String key : changes.keySet()) {
            if (key.startsWith(OUTPUT_SERVER_V2)
                || key.startsWith(TRACE_SENDER_TOPIC_V2)) {
                refresh = true;
                break;
            }
        }

        if (refresh) {
            try {
                this.sender.close();
            } catch (IOException e) {
                // ignored
            }
            this.config.updateConfigsNotNotify(changes);
            this.init(this.config);
        }
    }

    @Override
    public void close() throws IOException {
        if (this.sender != null) {
            this.sender.close();
        }
    }

    private boolean checkEnable(Config config) {
        boolean check = config.getBoolean(TRACE_SENDER_ENABLED_V2);
        if (check) {
            check = config.getBoolean(OUTPUT_SERVERS_ENABLE);
        } else {
            return false;
        }
        return check;
    }
}
