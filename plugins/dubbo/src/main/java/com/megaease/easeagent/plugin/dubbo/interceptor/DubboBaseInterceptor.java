package com.megaease.easeagent.plugin.dubbo.interceptor;

import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.dubbo.config.DubboTraceConfig;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

public abstract class DubboBaseInterceptor implements Interceptor {

	public static volatile DubboTraceConfig DUBBO_TRACE_CONFIG;

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		DUBBO_TRACE_CONFIG = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, ConfigConst.Namespace.DUBBO, this.getType(), DubboTraceConfig.SUPPLIER);
	}

}
