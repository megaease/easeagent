package com.megaease.easeagent.zipkin.impl;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Response;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class ProgressContextImpl implements ProgressContext {
    private final Tracing tracing;
    private final Span span;
    private final Scope scope;
    private final AsyncRequest asyncRequest;
    private final Supplier<InitializeContext> supplier;

    public ProgressContextImpl(Tracing tracing, Span span, Scope scope, AsyncRequest asyncRequest, Supplier<InitializeContext> supplier) {
        this.tracing = tracing;
        this.span = span;
        this.scope = scope;
        this.asyncRequest = asyncRequest;
        this.supplier = supplier;
    }

    @Override
    public Span span() {
        return span;
    }

    @Override
    public Scope scope() {
        return scope;
    }

    @Override
    public void setHeader(String name, String value) {
        asyncRequest.setHeader(name, value);
    }

    @Override
    public Map<String, String> getHeader() {
        return asyncRequest.getHeaders();
    }

    @Override
    public AsyncContext async() {
        return AsyncContextImpl.build(tracing, span, supplier, (Map) asyncRequest.getHeaders());
    }

    @Override
    public Context getContext() {
        return supplier.get();
    }

    @Override
    public void finish(Response response) {
        String[] fields = ProgressFields.getResponseHoldTagFields();
        if (!ProgressFields.isEmpty(fields)) {
            for (String field : fields) {
                span.tag(field, response.header(field));
            }
        }
        Set<String> keys = response.keys();
        if (keys != null && !keys.isEmpty()) {
            for (String key : keys) {
                span.tag(key, response.header(key));
            }
        }
        span.finish();
    }
}
