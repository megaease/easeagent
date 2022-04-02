package com.megaease.easeagent.plugin.api.otlp.common;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;

public class OtlpSpanContext {
    OtlpSpanContext() {}

    public static SpanContext getLogSpanContext() {
        Context context = EaseAgent.getContext();
        SpanContext spanContext;

        if (!context.currentTracing().hasCurrentSpan()) {
            spanContext = SpanContext.getInvalid();
        } else {
            Span span = context.currentTracing().currentSpan();
            spanContext = SpanContext.create(span.traceIdString(),
                span.spanIdString(),
                TraceFlags.getSampled(), TraceState.getDefault());

        }
        return spanContext;
    }
}
