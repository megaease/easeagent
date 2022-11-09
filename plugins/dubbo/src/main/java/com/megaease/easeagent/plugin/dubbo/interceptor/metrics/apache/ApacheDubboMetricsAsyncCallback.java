package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.apache;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboMetrics;
import org.apache.dubbo.rpc.Result;

import java.util.function.BiConsumer;

public class ApacheDubboMetricsAsyncCallback implements BiConsumer<Result,Throwable> {

    private AsyncContext asyncContext;

    private DubboMetrics dubboMetrics;

    public ApacheDubboMetricsAsyncCallback(AsyncContext asyncContext, DubboMetrics dubboMetrics) {
        this.asyncContext = asyncContext;
        this.dubboMetrics = dubboMetrics;
    }

    @Override
    public void accept(Result result, Throwable throwable) {
        try(Cleaner cleaner = asyncContext.importToCurrent()) {
            Context context = EaseAgent.getContext();
            Long duration = ContextUtils.getDuration(context);
            boolean callResult = ApacheDubboCtxUtils.checkCallResult(result, throwable);
            String service = context.get(ApacheDubboCtxUtils.METRICS_SERVICE_NAME);
            dubboMetrics.collect(service, duration, callResult);
        }
    }
}
