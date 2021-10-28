package com.megaease.easeagent.zipkin;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;

import java.util.function.Function;

public class RootSpanFinishHandler extends SpanHandler {
    private final Function rootSpanFinish;

    public RootSpanFinishHandler(Function rootSpanFinish) {
        this.rootSpanFinish = rootSpanFinish;
    }

    @Override
    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
        if (context.isLocalRoot()) {
            rootSpanFinish.apply(context);
        }
        return true;
    }
}
