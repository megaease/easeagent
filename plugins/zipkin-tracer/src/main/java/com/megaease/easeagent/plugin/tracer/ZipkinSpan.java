package com.megaease.easeagent.plugin.tracer;

import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ZipkinSpan implements Span {
    private static final Map<Kind, brave.Span.Kind> KINDS;

    static {
        Map<Kind, brave.Span.Kind> kinds = new HashMap<>();
        kinds.put(Kind.CLIENT, brave.Span.Kind.CLIENT);
        kinds.put(Kind.SERVER, brave.Span.Kind.SERVER);
        kinds.put(Kind.PRODUCER, brave.Span.Kind.PRODUCER);
        kinds.put(Kind.CONSUMER, brave.Span.Kind.CONSUMER);
        KINDS = Collections.unmodifiableMap(kinds);
    }

    private final brave.Span span;

    private ZipkinSpan(brave.Span span) {
        this.span = span;
    }

    public static Span build(brave.Span span) {
        if (span == null) {
            return NoOpTracer.NO_OP_SPAN;
        }
        return new ZipkinSpan(span);
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
    public void finish(long timestamp) {
        span.finish(timestamp);
    }

    @Override
    public void flush() {
        span.flush();
    }
}
