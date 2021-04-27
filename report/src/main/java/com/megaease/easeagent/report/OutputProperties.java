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

package com.megaease.easeagent.report;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;

import static com.megaease.easeagent.config.ConfigConst.Observability.*;

public interface OutputProperties {
    String getServers();

    String getTimeout();

    Boolean isEnabled();

    static OutputProperties newDefault(Configs configs) {
        return new Default(configs);
    }

    class Default implements OutputProperties {
        private volatile String servers;
        private volatile String timeout;
        private volatile boolean enabled;

        public Default(Configs configs) {
            ConfigUtils.bindProp(OUTPUT_SERVERS, configs, Config::getString, v -> this.servers = v);
            ConfigUtils.bindProp(OUTPUT_TIMEOUT, configs, Config::getString, v -> this.timeout = v);
            ConfigUtils.bindProp(OUTPUT_ENABLED, configs, Config::getBoolean, v -> this.enabled = v);
        }

        @Override
        public String getServers() {
            return this.servers;
        }

        @Override
        public String getTimeout() {
            return this.timeout;
        }

        @Override
        public Boolean isEnabled() {
            return this.enabled;
        }
    }
}
