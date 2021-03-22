package com.megaease.easeagent.metrics;

import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigConst;
import com.megaease.easeagent.config.ConfigUtils;

import static com.megaease.easeagent.config.ConfigConst.*;

public class MetricsCollectorConfig {
    private volatile boolean globalEnabled;
    private volatile boolean enabled;
    private volatile int interval;
    private Runnable callback;

    public MetricsCollectorConfig(Config config, String type) {
        ConfigUtils.bindProp(ConfigConst.Observability.METRICS_ENABLED, config, Config::getBoolean, v -> this.globalEnabled = v);
        ConfigUtils.bindProp(join(Observability.METRICS, type, Observability.KEY_COMM_ENABLED), config, Config::getBoolean, v -> this.enabled = v);
        ConfigUtils.bindProp(join(Observability.METRICS, type, Observability.KEY_COMM_INTERVAL), config, Config::getInt, v -> {
            this.interval = v;
            if (callback != null) {
                callback.run();
            }
        });
    }

    public boolean isEnabled() {
        return globalEnabled && enabled;
    }

    public int getInterval() {
        return interval;
    }

    public void setIntervalChangeCallback(Runnable runnable) {
        this.callback = runnable;
    }
}
