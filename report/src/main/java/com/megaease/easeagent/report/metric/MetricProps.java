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

package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.plugin.Const;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.utils.NoNull;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.KEY_COMM_APPEND_TYPE;
import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.KEY_COMM_TOPIC;

public interface MetricProps {
    String getName();

    String getAppendType();

    String getTopic();

    boolean isEnabled();

    void changeAppendType(String type);

    static MetricProps newDefault(IPluginConfig config) {
        return new Default(
            config.namespace(),
            config.enabled(),
            NoNull.of(config.getString(KEY_COMM_APPEND_TYPE), Const.METRIC_DEFAULT_APPEND_TYPE),
            NoNull.of(config.getString(KEY_COMM_TOPIC), Const.METRIC_DEFAULT_TOPIC)
        );
    }

    class Default implements MetricProps {
        private volatile String appendType;
        private final boolean enabled;
        private final String topic;
        private final String name;

        public Default(String name, boolean enabled, String appendType, String topic) {
            this.name = name;
            this.enabled = enabled;
            this.appendType = appendType;
            this.topic = topic;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getAppendType() {
            return this.appendType;
        }

        @Override
        public String getTopic() {
            return this.topic;
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public void changeAppendType(String type) {
            this.appendType = type;
        }
    }
}
