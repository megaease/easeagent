package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.ProviderInvoker;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.ProviderAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;

@AdviceTo(value = ProviderAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcProviderTraceInterceptor extends SofaRpcTraceBaseInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {

		ProviderInvoker<?> providerInvoker = (ProviderInvoker<?>)methodInfo.getInvoker();
		SofaRequest sofaRequest = (SofaRequest)methodInfo.getArgs()[0];
		SofaRpcCtxUtils.startServerSpan(context, providerInvoker, sofaRequest);
	}


	@Override
	public void after(MethodInfo methodInfo, Context context) {
		SofaResponse sofaResponse = (SofaResponse) methodInfo.getRetValue();
		SofaRpcCtxUtils.finishServerSpan(context, sofaResponse, methodInfo.getThrowable());
	}
}
