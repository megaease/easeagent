package com.megaease.easeagent.plugin.dubbo.interceptor.metrics.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;
import com.megaease.easeagent.plugin.dubbo.interceptor.metrics.DubboMetrics;

public class AlibabaDubboMetricsCallback implements ResponseCallback {
    private ResponseCallback responseCallback;
    private AsyncContext asyncContext;

    private DubboMetrics dubboMetrics;

    public AlibabaDubboMetricsCallback(ResponseCallback responseCallback, AsyncContext asyncContext, DubboMetrics dubboMetrics) {
        this.responseCallback = responseCallback;
        this.asyncContext = asyncContext;
        this.dubboMetrics = dubboMetrics;
    }

    @Override
    public void done(Object response) {
        try {
            responseCallback.done(response);
        } finally {
            process(response, null);
        }
    }

    @Override
    public void caught(Throwable exception) {
        try{
            responseCallback.caught(exception);
        } finally {
            process(null, exception);
        }
    }

    private void process(Object response, Throwable throwable ){
        try(Cleaner cleaner = asyncContext.importToCurrent()) {
            Context context = EaseAgent.getContext();
            boolean callResult = AlibabaDubboCtxUtils.checkCallResult(response, throwable);
            Long duration = ContextUtils.getDuration(context);
            String service = context.get(AlibabaDubboCtxUtils.METRICS_SERVICE_NAME);
            dubboMetrics.collect(service, duration, callResult);
        }
    }
}
