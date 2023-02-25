package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.common;

import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.ConsumerInvoker;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.ConsumerAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;

@AdviceTo(value = ConsumerAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcConsumerTraceInterceptor extends SofaRpcTraceBaseInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		Object[] args = methodInfo.getArgs();
		SofaRequest sofaRequest = (SofaRequest) args[0];
		ConsumerInvoker consumerInvoker = (ConsumerInvoker) methodInfo.getInvoker();
		SofaRpcCtxUtils.startClientSpan(context, sofaRequest, consumerInvoker);
	}


	@Override
	public void after(MethodInfo methodInfo, Context context) {
		SofaResponse sofaResponse = (SofaResponse) methodInfo.getRetValue();
		SofaRpcCtxUtils.finishClientSpan(context, sofaResponse, methodInfo.getThrowable());
	}
}
