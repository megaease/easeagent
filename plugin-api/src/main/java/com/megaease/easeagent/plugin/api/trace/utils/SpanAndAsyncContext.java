package com.megaease.easeagent.plugin.api.trace.utils;

import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Span;

public class SpanAndAsyncContext {
    private final Span span;
    private final AsyncContext asyncContext;

    private SpanAndAsyncContext(Span span, AsyncContext asyncContext) {
        this.span = span;
        this.asyncContext = asyncContext;
    }

    public static SpanAndAsyncContext build(Span span) {
        return new SpanAndAsyncContext(span, null);
    }

    public static SpanAndAsyncContext build(AsyncContext asyncContext) {
        return new SpanAndAsyncContext(null, asyncContext);
    }

    public Span getSpan() {
        return span;
    }

    public AsyncContext getAsyncContext() {
        return asyncContext;
    }
}
