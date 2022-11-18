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

    private static final String ARGS_COLLECT_ENABLED = "args.collect.enabled";
    private static final String RESULT_COLLECT_ENABLED = "result.collect.enabled";

    private volatile Boolean argsCollectEnabled = false;
    private volatile Boolean resultCollectEnabled = false;

    public Boolean argsCollectEnabled() {
        return argsCollectEnabled;
    }

    public Boolean resultCollectEnabled() {
        return resultCollectEnabled;
    }

    @Override
    public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
        String argsCollectEnabled = newConfig.getString(ARGS_COLLECT_ENABLED);
        this.argsCollectEnabled = Boolean.parseBoolean(argsCollectEnabled);

        String resultCollectEnabled = newConfig.getString(RESULT_COLLECT_ENABLED);
        this.resultCollectEnabled = Boolean.parseBoolean(resultCollectEnabled);
    }
}
