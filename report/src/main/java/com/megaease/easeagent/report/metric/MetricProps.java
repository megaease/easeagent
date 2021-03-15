package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;

import static com.megaease.easeagent.config.ConfigConst.*;

public interface MetricProps {
    String getAppendType();

    String getTopic();

    boolean isEnabled();

    static MetricProps newDefault(Configs configs, String key) {
        return new Default(configs, key);
    }

    class Default implements MetricProps {
        private volatile boolean enabled = false;
        private volatile String appendType;
        private volatile String topic;

        public Default(Configs configs, String key) {
            ConfigUtils.bindProp(join(METRICS, key, KEY_COMM_ENABLED), configs, Configs::getBoolean, v -> this.enabled = v);
            ConfigUtils.bindProp(join(METRICS, key, KEY_COMM_APPEND_TYPE), configs, Configs::getString, v -> this.appendType = v);
            ConfigUtils.bindProp(join(METRICS, key, KEY_COMM_TOPIC), configs, Configs::getString, v -> this.topic = v);
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