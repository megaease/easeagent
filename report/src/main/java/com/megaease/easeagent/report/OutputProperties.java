package com.megaease.easeagent.report;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;

import static com.megaease.easeagent.config.ConfigConst.*;

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
            ConfigUtils.bindProp(OUTPUT_SERVERS, configs, Configs::getString, v -> this.servers = v);
            ConfigUtils.bindProp(OUTPUT_TIMEOUT, configs, Configs::getString, v -> this.timeout = v);
            ConfigUtils.bindProp(OUTPUT_ENABLED, configs, Configs::getBoolean, v -> this.enabled = v);
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
