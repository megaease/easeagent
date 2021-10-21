package com.megaease.easeagent.plugin.tracer;

import brave.Span;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;

public class AsyncContextImpl implements AsyncContext {
    private final Tracing tracing;
    private final Span span;
    private final AsyncRequest request;

    public AsyncContextImpl(Tracing tracing, Span span, AsyncRequest request) {
        this.tracing = tracing;
        this.span = span;
        this.request = request;
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Tracing getTracer() {
        return tracing;
    }

    public Span getSpan() {
        return span;
    }

    @Override
    public Map<String, Object> getContext() {
        return null;
    }
}
