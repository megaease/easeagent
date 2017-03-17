package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import io.opentracing.Span;
import io.opentracing.Tracer;

import java.util.ArrayDeque;
import java.util.Deque;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public abstract class TraceContext {
    private static volatile Tracer tracer = null;

    private static final ThreadLocal<Deque<Span>> CURRENT_SPAN = new DequeThreadLocal();

    public static void init(Tracer tracer) {
        assert tracer != null;
        TraceContext.tracer = tracer;
    }

    public static Tracer tracer() {
        return tracer;
    }

    public static void push(Span span) {
        CURRENT_SPAN.get().push(span);
    }

    public static Span pop() {
        return CURRENT_SPAN.get().pop();
    }

    public static Span peek() {
        return CURRENT_SPAN.get().peek();
    }

    private TraceContext() { throw new UnsupportedOperationException();}

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    private static class DequeThreadLocal extends ThreadLocal<Deque<Span>> {
        @Override
        protected Deque<Span> initialValue() {
            return new ArrayDeque<Span>();
        }
    }
}
