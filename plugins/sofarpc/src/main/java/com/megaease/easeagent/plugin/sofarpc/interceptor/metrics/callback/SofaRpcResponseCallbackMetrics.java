package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.callback;

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcCtxUtils;
import com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.SofaRpcMetrics;

public class SofaRpcResponseCallbackMetrics implements SofaResponseCallback {
	private static final Logger LOG = EaseAgent.getLogger(SofaRpcResponseCallbackMetrics.class);
	private SofaResponseCallback sofaResponseCallback;
	private AsyncContext asyncContext;
	private SofaRpcMetrics sofaRpcMetrics;

	public SofaRpcResponseCallbackMetrics(SofaResponseCallback sofaResponseCallback, AsyncContext asyncContext, SofaRpcMetrics sofaRpcMetrics) {
		this.sofaResponseCallback = sofaResponseCallback;
		this.asyncContext = asyncContext;
		this.sofaRpcMetrics = sofaRpcMetrics;
	}

	@Override
	public void onAppResponse(Object appResponse, String methodName, RequestBase request) {
		try {
			this.sofaResponseCallback.onAppResponse(appResponse, methodName, request);
		} finally {
			SofaRpcCtxUtils.asyncFinishCollectMetrics(this.asyncContext, appResponse);
		}
	}

	@Override
	public void onAppException(Throwable throwable, String methodName, RequestBase request) {
		try {
			this.sofaResponseCallback.onAppException(throwable, methodName, request);
		} finally {
			SofaRpcCtxUtils.asyncFinishCollectMetrics(this.asyncContext, throwable);
		}
	}

	@Override
	public void onSofaException(SofaRpcException sofaException, String methodName, RequestBase request) {
		try {
			this.sofaResponseCallback.onSofaException(sofaException, methodName, request);
		} finally {
			SofaRpcCtxUtils.asyncFinishCollectMetrics(this.asyncContext, sofaException);
		}
	}
}
