package com.megaease.easeagent.plugin.dubbo.interceptor.trace.alibaba;

import com.alibaba.dubbo.remoting.exchange.ResponseCallback;
import com.alibaba.dubbo.rpc.Result;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.dubbo.AlibabaDubboCtxUtils;

public class AlibabaDubboTraceCallback implements ResponseCallback {
    private ResponseCallback responseCallback;
    private AsyncContext asyncContext;

    public AlibabaDubboTraceCallback(ResponseCallback responseCallback, AsyncContext asyncContext) {
        this.responseCallback = responseCallback;
        this.asyncContext = asyncContext;
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
            Result result = (Result) response;
            AlibabaDubboCtxUtils.finishSpan(context, result, throwable);
        }
    }
}
