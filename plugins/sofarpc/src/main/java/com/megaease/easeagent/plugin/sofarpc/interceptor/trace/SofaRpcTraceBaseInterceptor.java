package com.megaease.easeagent.plugin.sofarpc.interceptor.trace;

import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.sofarpc.config.SofaRpcTraceConfig;

public abstract class SofaRpcTraceBaseInterceptor implements NonReentrantInterceptor {
	public static volatile SofaRpcTraceConfig SOFA_RPC_TRACE_CONFIG;

	@Override
	public String getType() {
		return Order.TRACING.getName();
	}

	@Override
	public int order() {
		return Order.TRACING.getOrder();
	}

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		SOFA_RPC_TRACE_CONFIG = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, ConfigConst.Namespace.SOFARPC, this.getType(), SofaRpcTraceConfig.SUPPLIER);
	}

}
