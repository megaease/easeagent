package com.megaease.easeagent.plugin.sofarpc.interceptor.initalize;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.FutureInvokeCallbackConstructAdvice;
import com.megaease.easeagent.plugin.sofarpc.adivce.BoltFutureInvokeCallbackConstructAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;

@AdviceTo(value = FutureInvokeCallbackConstructAdvice.class, plugin = SofaRpcPlugin.class)
@AdviceTo(value = BoltFutureInvokeCallbackConstructAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcFutureInvokeCallbackConstructInterceptor extends SofaRpcTraceBaseInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		RequestContext requestContext = context.get(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);
		String methodSignature = context.get(SofaRpcCtxUtils.METRICS_KEY_NAME);
		if (requestContext != null) {
			try (Scope ignore = requestContext.scope()) {
				AgentDynamicFieldAccessor.setDynamicFieldValue(methodInfo.getArgs()[2], context.exportAsync());
			}
		} else if (methodSignature != null) {
			AgentDynamicFieldAccessor.setDynamicFieldValue(methodInfo.getArgs()[2], context.exportAsync());
		}
	}
}
