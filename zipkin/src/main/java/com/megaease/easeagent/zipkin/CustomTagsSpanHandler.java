package com.megaease.easeagent.zipkin;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;

import java.util.function.Supplier;

public class CustomTagsSpanHandler extends SpanHandler {
    public static final String TAG_INSTANCE = "i";
    private final String instance;
    private final Supplier<String> serviceName;

    public CustomTagsSpanHandler(Supplier<String> serviceName, String instance) {
        this.serviceName = serviceName;
        this.instance = instance;
    }

    @Override
    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
        span.tag(TAG_INSTANCE, this.instance);
        span.localServiceName(this.serviceName.get());
        return true;
    }
}
