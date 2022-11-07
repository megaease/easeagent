package com.megaease.easeagent.plugin.motan.interceptor.trace.consumer;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.Future;
import com.weibo.api.motan.rpc.FutureListener;

public class TraceFutureListener implements FutureListener {
	private AsyncContext asyncContext;

	public TraceFutureListener(AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

	@Override
	public void operationComplete(Future future) throws Exception {
		try(Cleaner cleaner = asyncContext.importToCurrent()){
			MotanCtxUtils.finishConsumerSpan(future, EaseAgent.getContext());
		}
	}
}
