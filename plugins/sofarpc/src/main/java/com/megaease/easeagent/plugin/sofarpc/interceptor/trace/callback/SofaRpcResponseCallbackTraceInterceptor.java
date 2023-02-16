package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.callback;

import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcPlugin;
import com.megaease.easeagent.plugin.sofarpc.adivce.ResponseCallbackAdvice;
import com.megaease.easeagent.plugin.sofarpc.interceptor.trace.SofaRpcTraceBaseInterceptor;

@AdviceTo(value = ResponseCallbackAdvice.class, plugin = SofaRpcPlugin.class)
public class SofaRpcResponseCallbackTraceInterceptor extends SofaRpcTraceBaseInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		SofaResponseCallback sofaResponseCallback = (SofaResponseCallback) methodInfo.getArgs()[2];

		RequestContext requestContext = (RequestContext) context.get(SofaRpcCtxUtils.CLIENT_REQUEST_CONTEXT_KEY);
		try (Scope scope = requestContext.scope()) {
			AsyncContext asyncContext = context.exportAsync();
			methodInfo.changeArg(2, new SofaRpcResponseCallbackTrace(sofaResponseCallback, asyncContext));
		}
	}
}
