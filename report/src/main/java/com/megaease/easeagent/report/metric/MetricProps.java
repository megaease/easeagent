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

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.Const;
import com.megaease.easeagent.plugin.utils.NoNull;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.*;
import static com.megaease.easeagent.plugin.api.config.ConfigConst.SERVICE_ID_ENABLED_KEY;
import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.*;

public interface MetricProps {
    String getAppendType();

    String getTopic();

    boolean isEnabled();

    static MetricProps newDefault(Configs configs, String key) {
        return new Default(configs, key);
    }

    static MetricProps newDefault(com.megaease.easeagent.plugin.api.config.Config config) {
        return new Default(
            config.enabled(),
            NoNull.of(config.getString(KEY_COMM_APPEND_TYPE), Const.METRIC_DEFAULT_APPEND_TYPE),
            NoNull.of(config.getString(KEY_COMM_TOPIC), Const.METRIC_DEFAULT_TOPIC)
        );
    }

    class Default implements MetricProps {
        private volatile boolean enabled = false;
        private volatile String appendType;
        private volatile String topic;

        public Default(Configs configs, String key) {
            ConfigUtils.bindProp(join(METRICS, key, SERVICE_ID_ENABLED_KEY), configs, Config::getBoolean, v -> this.enabled = v);
            ConfigUtils.bindProp(join(METRICS, key, KEY_COMM_APPEND_TYPE), configs, Config::getString, v -> this.appendType = v);
            ConfigUtils.bindProp(join(METRICS, key, KEY_COMM_TOPIC), configs, Config::getString, v -> this.topic = v);
        }

        public Default(boolean enabled, String appendType, String topic) {
            this.enabled = enabled;
            this.appendType = appendType;
            this.topic = topic;
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
    }
}
