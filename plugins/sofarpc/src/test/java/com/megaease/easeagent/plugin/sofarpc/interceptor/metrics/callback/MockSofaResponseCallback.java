package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics.callback;

import com.alipay.sofa.rpc.core.exception.SofaRpcException;
import com.alipay.sofa.rpc.core.invoke.SofaResponseCallback;
import com.alipay.sofa.rpc.core.request.RequestBase;

class MockSofaResponseCallback implements SofaResponseCallback<Object> {
	private Object result;

	public Object getResult() {
		return result;
	}

	@Override
	public void onAppResponse(Object appResponse, String methodName, RequestBase request) {
		this.result = appResponse;
	}

	@Override
	public void onAppException(Throwable throwable, String methodName, RequestBase request) {
		this.result = throwable;
	}

	@Override
	public void onSofaException(SofaRpcException sofaException, String methodName, RequestBase request) {
		this.result = sofaException;
	}
}
