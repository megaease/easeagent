package com.megaease.easeagent.plugin.motan.config;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfig;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;

public class MotanPluginConfig implements AutoRefreshPluginConfig {
    public static final AutoRefreshConfigSupplier<MotanPluginConfig> SUPPLIER = new AutoRefreshConfigSupplier<MotanPluginConfig>(){
        @Override
        public MotanPluginConfig newInstance() {
            return new MotanPluginConfig();
        }
    };

    public static final String ARGS_COLLECT_ENABLED = "args.collect.enabled";

    private volatile Boolean argsCollectEnabled = false;

    public Boolean argsCollectEnabled() {
        return argsCollectEnabled;
    }

    @Override
    public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
        String argsCollectEnabled = newConfig.getString(ARGS_COLLECT_ENABLED);
        this.argsCollectEnabled = Boolean.parseBoolean(argsCollectEnabled);
    }
}
