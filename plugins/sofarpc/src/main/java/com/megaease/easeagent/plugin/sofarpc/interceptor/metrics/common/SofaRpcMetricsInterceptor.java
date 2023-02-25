package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.common;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.ConsumerAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetricsBaseInterceptor;

@AdviceTo(value = ConsumerAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcMetricsInterceptor extends SofaRpcMetricsBaseInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		SofaRequest sofaRequest = (SofaRequest) methodInfo.getArgs()[0];
		SofaRpcCtxUtils.startCollectMetrics(context, sofaRequest);
	}


	@Override
	public void after(MethodInfo methodInfo, Context context) {
		SofaResponse retValue = (SofaResponse) methodInfo.getRetValue();
		SofaRpcCtxUtils.finishCollectMetrics(context, retValue, methodInfo.getThrowable());
	}
}
