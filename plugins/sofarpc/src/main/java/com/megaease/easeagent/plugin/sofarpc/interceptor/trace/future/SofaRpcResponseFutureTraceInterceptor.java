package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.future;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.ResponseFutureAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;

@AdviceTo(value = ResponseFutureAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcResponseFutureTraceInterceptor extends SofaRpcTraceBaseInterceptor {

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		Object result = methodInfo.getArgs()[0];
		AsyncContext asyncContext = AgentDynamicFieldAccessor.getDynamicFieldValue(methodInfo.getInvoker());

		if (methodInfo.getThrowable() != null) {
			result = methodInfo.getThrowable();
		}
		SofaRpcCtxUtils.asyncFinishClientSpan(asyncContext, result);
	}
}
