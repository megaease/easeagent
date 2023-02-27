package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.motan.interceptor.MotanCtxUtils;
import com.weibo.api.motan.rpc.Future;
import com.weibo.api.motan.rpc.FutureListener;

import static com.megaease.easeagent.plugin.motan.interceptor.metrics.MotanBaseMetricsInterceptor.MOTAN_METRIC;

public class MetricsFutureListener implements FutureListener {
	private final AsyncContext asyncContext;


	public MetricsFutureListener(AsyncContext asyncContext) {
		this.asyncContext = asyncContext;
	}

	@Override
	public void operationComplete(Future future) throws Exception {
		try(Cleaner cleaner = asyncContext.importToCurrent()){
			Context context = EaseAgent.getContext();
			Long duration = ContextUtils.getDuration(context,MotanCtxUtils.BEGIN_TIME);
			String service = context.get(MotanCtxUtils.METRICS_SERVICE_NAME);
			boolean callResult = future.getException() == null;
            MOTAN_METRIC.collectMetric(service, duration, callResult);
		}
	}
}
