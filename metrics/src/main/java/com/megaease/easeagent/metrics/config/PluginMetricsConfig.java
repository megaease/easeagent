package com.megaease.easeagent.metrics.config;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import static com.megaease.easeagent.config.ConfigConst.Observability.KEY_COMM_ENABLED;
import static com.megaease.easeagent.config.ConfigConst.Observability.KEY_COMM_INTERVAL;

public class PluginMetricsConfig implements MetricsConfig {
    private volatile boolean enabled;
    private volatile int interval;
    private Runnable callback;

    public PluginMetricsConfig(Config config) {
        set(config);
        config.addChangeListener(new ConfigChange());
    }


    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getInterval() {
        return interval;
    }

    @Override
    public void setIntervalChangeCallback(Runnable runnable) {
        this.callback = runnable;
    }

    private void set(Config config) {
        this.enabled = config.getBoolean(KEY_COMM_ENABLED);
        this.interval = config.getInt(KEY_COMM_INTERVAL);
    }

    class ConfigChange implements ConfigChangeListener {

        @Override
        public void onChange(Config oldConfig, Config newConfig) {
            int oldInterval = PluginMetricsConfig.this.interval;
            set(newConfig);
            if (oldInterval != PluginMetricsConfig.this.interval) {
                callback.run();
            }
        }
    }
}
