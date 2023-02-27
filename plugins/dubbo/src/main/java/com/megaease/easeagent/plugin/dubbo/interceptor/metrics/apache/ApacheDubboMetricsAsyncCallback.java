package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.apache;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import org.apache.dubbo.rpc.Result;

import java.util.function.BiConsumer;

import static com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboBaseMetricsInterceptor.DUBBO_METRICS;

public class ApacheDubboMetricsAsyncCallback implements BiConsumer<Result,Throwable> {

    private final AsyncContext asyncContext;

    public ApacheDubboMetricsAsyncCallback(AsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    public void accept(Result result, Throwable throwable) {
        try(Cleaner cleaner = asyncContext.importToCurrent()) {
            Context context = EaseAgent.getContext();
            Long duration = ContextUtils.getDuration(context,ApacheDubboCtxUtils.BEGIN_TIME);
            boolean callResult = ApacheDubboCtxUtils.checkCallResult(result, throwable);
            String service = context.get(ApacheDubboCtxUtils.METRICS_SERVICE_NAME);
            DUBBO_METRICS.collect(service, duration, callResult);
        }
    }
}
