package com.megaease.easeagent.zipkin.impl;

import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.Span;

import java.util.Map;

public class ProgressContextImpl implements ProgressContext {
    private final Span span;
    private final AsyncRequest asyncRequest;

    public ProgressContextImpl(Span span, AsyncRequest asyncRequest) {
        this.span = span;
        this.asyncRequest = asyncRequest;
    }

    @Override
    public Span span() {
        return span;
    }

    @Override
    public Map<String, String> getHeader() {
        return asyncRequest.getHeader();
    }
}
