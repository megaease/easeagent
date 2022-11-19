package com.megaease.easeagent.plugin.dubbo.interceptor.trace.apache;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.dubbo.ApacheDubboCtxUtils;
import org.apache.dubbo.rpc.Result;

import java.util.function.BiConsumer;

public class ApacheDubboTraceCallback implements BiConsumer<Result, Throwable> {
    private Span span;

    public ApacheDubboTraceCallback(Span span) {
        this.span = span;
    }

    @Override
    public void accept(Result result, Throwable throwable) {
        ApacheDubboCtxUtils.doFinishSpan(span, result, throwable);
    }
}
