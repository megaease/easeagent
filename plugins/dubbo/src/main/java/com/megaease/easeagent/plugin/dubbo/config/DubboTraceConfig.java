package com.megaease.easeagent.plugin.dubbo.config;

import com.megaease.easeagent.plugin.api.config.AutoRefreshConfigSupplier;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfig;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.utils.Pair;

public class DubboTraceConfig implements AutoRefreshPluginConfig {

	private final Pair<String, Boolean> argsCollectEnabledPair = new Pair<>("args.collect.enabled", false);
	private volatile boolean argsCollectEnabled = argsCollectEnabledPair.getValue();

	public boolean argsCollectEnabled() {
		return argsCollectEnabled;
	}

	public static final AutoRefreshConfigSupplier<DubboTraceConfig> SUPPLIER = new AutoRefreshConfigSupplier<DubboTraceConfig>() {
		@Override
		public DubboTraceConfig newInstance() {
			return new DubboTraceConfig();
		}
	};


	@Override
	public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
		String argsCollectEnabled = newConfig.getString(argsCollectEnabledPair.getKey());
		this.argsCollectEnabled = Boolean.parseBoolean(argsCollectEnabled);
	}
}
