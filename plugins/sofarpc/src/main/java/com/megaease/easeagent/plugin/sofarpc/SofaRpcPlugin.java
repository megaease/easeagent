package com.megaease.easeagent.plugin.sofarpc;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class SofaRpcPlugin implements AgentPlugin {
	@Override
	public String getNamespace() {
		return ConfigConst.Namespace.SOFARPC;
	}

	@Override
	public String getDomain() {
		return ConfigConst.OBSERVABILITY;
	}
}
