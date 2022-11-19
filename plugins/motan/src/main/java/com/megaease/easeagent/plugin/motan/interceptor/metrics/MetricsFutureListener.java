package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.Future;
import com.weibo.api.motan.rpc.FutureListener;

public class MetricsFutureListener implements FutureListener {
	private MotanMetric motanMetric;
	private AsyncContext asyncContext;


	public MetricsFutureListener(MotanMetric motanMetric, AsyncContext asyncContext) {
		this.motanMetric = motanMetric;
		this.asyncContext = asyncContext;
	}

	@Override
	public void operationComplete(Future future) throws Exception {
		try(Cleaner cleaner = asyncContext.importToCurrent()){
			Context context = EaseAgent.getContext();
			Long duration = ContextUtils.getDuration(context);
			String service = context.get(MotanCtxUtils.METRICS_SERVICE_NAME);
			boolean callResult = future.getException() == null;
			motanMetric.collectMetric(service, duration, callResult);
		}
	}
}
