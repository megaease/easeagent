package com.megaease.easeagent.plugin.sofarpc.interceptor.trace.callback;

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;

public class SofaRpcResponseCallbackTrace implements SofaResponseCallback<Object> {
	private final SofaResponseCallback<?> sofaResponseCallback;
	private final AsyncContext asyncContext;

	public SofaRpcResponseCallbackTrace(SofaResponseCallback<?> sofaResponseCallback, AsyncContext asyncContext) {
		this.sofaResponseCallback = sofaResponseCallback;
		this.asyncContext = asyncContext;
	}

	@Override
	public void onAppResponse(Object appResponse, String methodName, RequestBase request) {
		try {
			this.sofaResponseCallback.onAppResponse(appResponse, methodName, request);
		} finally {
			SofaRpcCtxUtils.asyncFinishClientSpan(this.asyncContext, appResponse);
		}
	}

	@Override
	public void onAppException(Throwable throwable, String methodName, RequestBase request) {
		try {
			this.sofaResponseCallback.onAppException(throwable, methodName, request);
		} finally {
			SofaRpcCtxUtils.asyncFinishClientSpan(this.asyncContext, throwable);
		}
	}

	@Override
	public void onSofaException(SofaRpcException sofaException, String methodName, RequestBase request) {
		try {
			this.sofaResponseCallback.onSofaException(sofaException, methodName, request);
		} finally {
			SofaRpcCtxUtils.asyncFinishClientSpan(this.asyncContext, sofaException);
		}
	}
}
