package com.megaease.easeagent.plugin.sofarpc.config;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfig;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.utils.Pair;

public class SofaRpcTraceConfig implements AutoRefreshPluginConfig {

	private final Pair<String, Boolean> argsCollectEnabledPair = new Pair<>("args.collect.enabled", false);
    private final Pair<String, Boolean> resultCollectEnabledPair = new Pair<>("result.collect.enabled", false);
	private volatile boolean argsCollectEnabled = argsCollectEnabledPair.getValue();
    private volatile boolean resultCollectEnabled = resultCollectEnabledPair.getValue();

	public boolean argsCollectEnabled() {
		return argsCollectEnabled;
	}

    public boolean resultCollectEnabled() {
        return resultCollectEnabled;
    }

    public static final AutoRefreshConfigSupplier<SofaRpcTraceConfig> SUPPLIER = new AutoRefreshConfigSupplier<SofaRpcTraceConfig>() {
		@Override
		public SofaRpcTraceConfig newInstance() {
			return new SofaRpcTraceConfig();
		}
	};

	@Override
	public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
		String argsCollectEnabled = newConfig.getString(argsCollectEnabledPair.getKey());
		this.argsCollectEnabled = Boolean.parseBoolean(argsCollectEnabled);

        String resultCollectEnabled = newConfig.getString(resultCollectEnabledPair.getKey());
        this.resultCollectEnabled = Boolean.parseBoolean(resultCollectEnabled);
    }
}
