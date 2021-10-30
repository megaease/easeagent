package com.megaease.easeagent.zipkin.impl;

import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class SpanImpl implements Span {
    private static final Map<Kind, brave.Span.Kind> KINDS;

    static {
        Map<Kind, brave.Span.Kind> kinds = new HashMap<>();
        kinds.put(Kind.CLIENT, brave.Span.Kind.CLIENT);
        kinds.put(Kind.SERVER, brave.Span.Kind.SERVER);
        kinds.put(Kind.PRODUCER, brave.Span.Kind.PRODUCER);
        kinds.put(Kind.CONSUMER, brave.Span.Kind.CONSUMER);
        KINDS = Collections.unmodifiableMap(kinds);
    }

    private final Tracing tracing;
    private final brave.Span span;
    private CurrentTraceContext.Scope scope;
    private final TraceContext.Injector<Request> injector;

    private SpanImpl(@Nonnull Tracing tracing, @Nonnull brave.Span span, @Nonnull TraceContext.Injector<Request> injector) {
        this.tracing = tracing;
        this.span = span;
        this.injector = injector;
    }

    public static Span build(Tracing tracing, brave.Span span, TraceContext.Injector<Request> injector) {
        if (span == null) {
            return NoOpTracer.NO_OP_SPAN;
        }
        return new SpanImpl(tracing, span, injector);
    }

    public static brave.Span.Kind braveKind(Kind kind) {
        return KINDS.get(kind);
    }

    protected brave.Span getSpan() {
        return span;
    }

    @Override
    public Span name(String name) {
        span.name(name);
        return this;
    }

    @Override
    public Span tag(String key, String value) {
        span.tag(key, value);
        return this;
    }

    @Override
    public Span annotate(String value) {
        span.annotate(value);
        return this;
    }

    @Override
    public boolean isNoop() {
        return span.isNoop();
    }

    @Override
    public Span start() {
        span.start();
        return this;
    }

    @Override
    public Span start(long timestamp) {
        span.start(timestamp);
        return this;
    }

    @Override
    public Span kind(@Nullable Kind kind) {
        span.kind(KINDS.get(kind));
        return this;
    }

    @Override
    public Span annotate(long timestamp, String value) {
        span.annotate(timestamp, value);
        return this;
    }

    @Override
    public Span error(Throwable throwable) {
        span.error(throwable);
        return this;
    }

    @Override
    public Span remoteServiceName(String remoteServiceName) {
        span.remoteServiceName(remoteServiceName);
        return this;
    }

    @Override
    public boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort) {
        span.remoteIpAndPort(remoteIp, remotePort);
        return false;
    }

    @Override
    public void abandon() {
        span.abandon();
    }

    @Override
    public void finish() {
        finish(System.currentTimeMillis());
    }

    @Override
    public void finish(long timestamp) {
        if (scope != null) {
            scope.close();
        }
        span.finish(timestamp);
    }

    @Override
    public void flush() {
        span.flush();
    }

    @Override
    public void inject(Request request) {
        injector.inject(span.context(), request);
    }

    @Override
    public Span maybeScope() {
        if (scope != null) {
            return this;
        }
        scope = tracing.currentTraceContext().maybeScope(span.context());
        return this;
    }
}
