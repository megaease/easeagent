package com.megaease.easeagent.plugin.dubbo;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

public class DubboPlugin implements AgentPlugin {
	@Override
	public String getNamespace() {
		return ConfigConst.Namespace.DUBBO;
	}

	@Override
	public String getDomain() {
		return ConfigConst.OBSERVABILITY;
	}
}
