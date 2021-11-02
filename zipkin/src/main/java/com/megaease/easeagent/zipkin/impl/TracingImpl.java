package com.megaease.easeagent.zipkin.impl;

import brave.Tracer;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpContext;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class TracingImpl implements Tracing {
    private final Supplier<InitializeContext> supplier;
    private final brave.Tracing tracing;
    private final brave.Tracer tracer;
    private final TraceContext.Injector<Request> defaultInjector;
    private final TraceContext.Extractor<Request> defaultExtractor;

    private TracingImpl(@Nonnull Supplier<InitializeContext> supplier,
                        @Nonnull brave.Tracing tracing,
                        @Nonnull Tracer tracer,
                        @Nonnull TraceContext.Injector<Request> defaultInjector,
                        @Nonnull TraceContext.Extractor<Request> defaultExtractor) {
        this.supplier = supplier;
        this.tracing = tracing;
        this.tracer = tracer;
        this.defaultInjector = defaultInjector;
        this.defaultExtractor = defaultExtractor;
    }

    public static Tracing build(Supplier<InitializeContext> supplier, brave.Tracing tracing) {
        tracing.sampler();
        return tracing == null ? NoOpTracer.NO_OP_TRACING :
            new TracingImpl(supplier, tracing,
                tracing.tracer(),
                tracing.propagation().injector(Request::setHeader),
                tracing.propagation().extractor(Request::header)
            );
    }

    @Override
    public boolean isNoop() {
        return false;
    }


    private brave.Tracer tracer() {
        return this.tracer;
    }

    private brave.Tracing tracing() {
        return this.tracing;
    }

    @Override
    public Span currentSpan() {
        brave.Tracer tracer = tracer();
        Span span = NoOpTracer.NO_OP_SPAN;
        if (tracer != null) {
            span = build(tracer.currentSpan());
        }
        return NoOpTracer.noNullSpan(span);
    }

    private Span build(brave.Span bSpan) {
        return build(bSpan, false);
    }

    private Span build(brave.Span bSpan, boolean cacheScope) {
        Span span = SpanImpl.build(tracing(), bSpan, defaultInjector);
        if (cacheScope) {
            span.cacheScope();
        }
        return span;
    }

    private void setInfo(brave.Span span, Request request) {
        Span.Kind kind = request.kind();
        if (kind != null) {
            span.kind(SpanImpl.braveKind(kind));
        }
        span.name(request.name());
    }

    @Override
    public AsyncContext exportAsync(Request request) {
        brave.Span span = tracer().nextSpan();
        defaultInjector.inject(span.context(), request);
        return new AsyncContextImpl(this, build(span, request.cacheScope()), supplier);
    }

    @Override
    public Span importAsync(AsyncContext snapshot) {
        Span span = null;
        if (snapshot instanceof AsyncContextImpl) {
            Span bSpan = ((AsyncContextImpl) snapshot).getSpan();
            if (bSpan.isNoop()) {
                return NoOpTracer.NO_OP_SPAN;
            }
            bSpan.start();
            span = bSpan;
        }
        return NoOpTracer.noNullSpan(span);
    }

    @Override
    public ProgressContext nextProgress(Request request) {
        TraceContext maybeParent = tracing.currentTraceContext().get();
        // Unlike message consumers, we try current span before trying extraction. This is the proper
        // order because the span in scope should take precedence over a potentially stale header entry.
        //
        brave.Span span;
        if (maybeParent == null) {
            TraceContextOrSamplingFlags extracted = defaultExtractor.extract(request);
            span = tracer().nextSpan(extracted);
        } else { // If we have a span in scope assume headers were cleared before
            span = tracer.newChild(maybeParent);
        }
        if (span.isNoop()) {
            return NoOpContext.NO_OP_PROGRESS_CONTEXT;
        }
        setInfo(span, request);
        AsyncRequest asyncRequest = new AsyncRequest(request);
        defaultInjector.inject(span.context(), asyncRequest);
        Span newSpan = build(span, request.cacheScope());
        return new ProgressContextImpl(this, newSpan, newSpan.maybeScope(), asyncRequest, supplier);
    }

    @Override
    public ProgressContext importProgress(Request request) {
        TraceContextOrSamplingFlags extracted = defaultExtractor.extract(request);
        brave.Span span = tracer().nextSpan(extracted);
        if (span.isNoop()) {
            return NoOpContext.NO_OP_PROGRESS_CONTEXT;
        }
        setInfo(span, request);
        AsyncRequest asyncRequest = new AsyncRequest(request);
        defaultInjector.inject(span.context(), asyncRequest);
        Span newSpan = build(span, request.cacheScope());
        return new ProgressContextImpl(this, newSpan, newSpan.maybeScope(), asyncRequest, supplier);
    }

    @Override
    public Span nextSpan() {
        return nextSpan(null);
    }


    @Override
    public Span nextSpan(Message message) {
        Object msg = message == null ? null : message.get();
        Span span = null;
        if (msg == null) {
            span = build(tracer().nextSpan(), true);
        } else if (msg instanceof TraceContextOrSamplingFlags) {
            span = build(tracer().nextSpan((TraceContextOrSamplingFlags) msg), true);
        }
        return NoOpTracer.noNullSpan(span);
    }

    @Override
    public MessagingTracing<? extends Request> messagingTracing() {
        return MessagingTracingImpl.build(tracing());
    }

}
