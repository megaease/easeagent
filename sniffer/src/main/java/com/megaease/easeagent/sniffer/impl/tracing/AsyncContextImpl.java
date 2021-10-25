package com.megaease.easeagent.sniffer.impl.tracing;

import brave.Span;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.HashMap;
import java.util.Map;

public class AsyncContextImpl implements AsyncContext {
    private final Tracing tracing;
    private final Span span;
    private final AsyncRequest request;
    private final Map<Object, Object> context;

    public AsyncContextImpl(Tracing tracing, Span span, AsyncRequest request) {
        this.tracing = tracing;
        this.span = span;
        this.request = request;
        this.context = new HashMap<>();
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
    public Map<Object, Object> getContext() {
        return context;
    }

    @Override
    public void putContext(Map<Object, Object> context) {
        context.putAll(context);
    }
}
