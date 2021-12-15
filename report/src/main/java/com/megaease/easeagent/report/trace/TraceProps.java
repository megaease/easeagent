/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.report.trace;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfig;
import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigImpl;
import com.megaease.easeagent.plugin.api.config.AutoRefreshRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.*;

public interface TraceProps {

    boolean isEnabled();

    KafkaOutputProps getOutput();

    interface KafkaOutputProps {

        boolean isEnabled();

        int getReportThread();

        int getMessageMaxBytes();

        String getTopic();

        int getQueuedMaxSpans();

        long getMessageTimeout();

        int getQueuedMaxSize();
    }

    static TraceProps newDefault(Configs configs) {
        return new Default(configs);
    }

    class Default implements TraceProps {
        private final KafkaOutputProps output;
        private final AutoRefreshConfigImpl autoRefreshConfig;

        public Default(Configs configs) {
            this.autoRefreshConfig = AutoRefreshRegistry.getOrCreate(ConfigConst.OBSERVABILITY, ConfigConst.PLUGIN_GLOBAL, ConfigConst.TRACING_SERVICE_ID);
            this.output = new KafkaOutputPropsImpl(configs);
        }


        @Override
        public boolean isEnabled() {
            return autoRefreshConfig.enabled();
        }

        @Override
        public KafkaOutputProps getOutput() {
            return output;
        }

        class KafkaOutputPropsImpl implements KafkaOutputProps {
            private volatile boolean enabled;
            private volatile String topic;
            private volatile int messageMaxBytes;
            private volatile int reportThread;
            private volatile int queuedMaxSpans;
            private volatile int queuedMaxSize;
            private volatile int messageTimeout;

            public KafkaOutputPropsImpl(Configs configs) {
                ConfigUtils.bindProp(TRACE_OUTPUT_ENABLED, configs, Config::getBoolean, v -> this.enabled = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_TOPIC, configs, Config::getString, v -> this.topic = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_MESSAGE_MAX_BYTES, configs, Config::getInt, v -> this.messageMaxBytes = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_REPORT_THREAD, configs, Config::getInt, v -> this.reportThread = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_QUEUED_MAX_SPANS, configs, Config::getInt, v -> this.queuedMaxSpans = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_QUEUED_MAX_SIZE, configs, Config::getInt, v -> this.queuedMaxSize = v);
                ConfigUtils.bindProp(TRACE_OUTPUT_MESSAGE_TIMEOUT, configs, Config::getInt, v -> this.messageTimeout = v);
            }

            @Override
            public boolean isEnabled() {
                return this.enabled;
            }

            @Override
            public int getReportThread() {
                return this.reportThread;
            }

            @Override
            public int getMessageMaxBytes() {
                return this.messageMaxBytes;
            }

            @Override
            public String getTopic() {
                return this.topic;
            }

            @Override
            public int getQueuedMaxSpans() {
                return this.queuedMaxSpans;
            }

            @Override
            public long getMessageTimeout() {
                return this.messageTimeout;
            }

            @Override
            public int getQueuedMaxSize() {
                return this.queuedMaxSize;
            }
        }
    }
}
