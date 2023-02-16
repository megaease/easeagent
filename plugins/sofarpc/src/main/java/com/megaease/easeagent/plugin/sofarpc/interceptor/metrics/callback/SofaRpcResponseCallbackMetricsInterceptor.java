package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.callback;

import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.ResponseCallbackAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetricsBaseInterceptor;

@AdviceTo(value = ResponseCallbackAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcResponseCallbackMetricsInterceptor extends SofaRpcMetricsBaseInterceptor {

	private static final Logger LOG = EaseAgent.getLogger(SofaRpcResponseCallbackMetricsInterceptor.class);

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		SofaResponseCallback sofaResponseCallback = (SofaResponseCallback) methodInfo.getArgs()[2];
		methodInfo.changeArg(2, new SofaRpcResponseCallbackMetrics(sofaResponseCallback, context.exportAsync(), SOFARPC_METRICS));
	}

}
