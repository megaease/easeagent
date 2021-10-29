package com.megaease.easeagent.zipkin.impl;

import brave.Span;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AsyncContextImpl implements AsyncContext {
    private final Tracing tracing;
    private final Span span;
    private final AsyncRequest request;
    private final Map<Object, Object> context;
    private final Supplier<Context> supplier;

    public AsyncContextImpl(Tracing tracing, Span span, AsyncRequest request, Supplier<Context> supplier) {
        this.tracing = tracing;
        this.span = span;
        this.request = request;
        this.supplier = supplier;
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

    @Override
    public Context getContext() {
        return supplier.get();
    }

    @Override
    public com.megaease.easeagent.plugin.api.trace.Span importToCurr() {
        return supplier.get().importAsync(this);
    }

    public Span getSpan() {
        return span;
    }

    @Override
    public Map<Object, Object> getAll() {
        return context;
    }

    @Override
    public void putAll(Map<Object, Object> context) {
        context.putAll(context);
    }
}
