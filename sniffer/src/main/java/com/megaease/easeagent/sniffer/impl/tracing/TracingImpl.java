package com.megaease.easeagent.sniffer.impl.tracing;

import brave.Tracer;
import brave.propagation.ThreadLocalSpan;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import brave.sampler.Sampler;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpContext;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class TracingImpl implements Tracing {
    private final brave.Tracing tracing;
    private final brave.Tracer tracer;
    private final Sampler sampler;
    private final TraceContext.Injector<Request> defaultInjector;
    private final TraceContext.Extractor<Request> defaultExtractor;

    private TracingImpl(@Nonnull brave.Tracing tracing,
                        @Nonnull Tracer tracer,
                        @Nonnull Sampler sampler,
                        @Nonnull TraceContext.Injector<Request> defaultInjector,
                        @Nonnull TraceContext.Extractor<Request> defaultExtractor) {
        this.tracing = tracing;
        this.tracer = tracer;
        this.sampler = sampler;
        this.defaultInjector = defaultInjector;
        this.defaultExtractor = defaultExtractor;
    }

    public static Tracing build(brave.Tracing tracing) {
        tracing.sampler();
        return tracing == null ? NoOpTracer.NO_OP_TRACING :
            new TracingImpl(tracing,
                tracing.tracer(),
                tracing.sampler(),
                tracing.propagation().injector(Request::setHeader),
                tracing.propagation().extractor(Request::header)
            );
    }

    @Override
    public boolean isNoop() {
        return false;
    }


    private brave.Tracer tracer() {
        return this.tracer == null ? brave.Tracing.currentTracer() : this.tracer;
    }

    private brave.Tracing tracing() {
        return this.tracing == null ? brave.Tracing.current() : this.tracing;
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

    private Span build(brave.Span bSpan, boolean inScope) {
        return SpanImpl.build(bSpan, inScope ? tracer().withSpanInScope(bSpan) : null, defaultInjector);
    }

    @Override
    public AsyncContext exportAsync(Request request) {
        AsyncRequest asyncRequest = new AsyncRequest(request);
        brave.Span span = tracer().nextSpan();
        defaultInjector.inject(span.context(), asyncRequest);
        return new AsyncContextImpl(this, span, asyncRequest);
    }

    @Override
    public Span importAsync(AsyncContext snapshot) {
        Span span = null;
        if (snapshot instanceof AsyncContextImpl) {
            brave.Span bSpan = ((AsyncContextImpl) snapshot).getSpan();
            if (bSpan.isNoop()) {
                return NoOpTracer.NO_OP_SPAN;
            }
            bSpan.start();
            span = build(bSpan);
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
        AsyncRequest asyncRequest = new AsyncRequest(request);
        defaultInjector.inject(span.context(), asyncRequest);
        return new ProgressContextImpl(build(span, true), asyncRequest);
    }

    @Override
    public Span importProgress(Request request) {
        TraceContextOrSamplingFlags extracted = defaultExtractor.extract(request);
        brave.Span span = tracer().nextSpan(extracted);
        if (span.isNoop()) {
            return NoOpTracer.NO_OP_SPAN;
        }
        defaultInjector.inject(span.context(), request);
        return build(span, true);
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
            span = build(ThreadLocalSpan.CURRENT_TRACER.next());
        } else if (msg instanceof TraceContextOrSamplingFlags) {
            span = build(ThreadLocalSpan.CURRENT_TRACER.next((TraceContextOrSamplingFlags) msg));
        }
        return NoOpTracer.noNullSpan(span);
    }

    @Override
    public MessagingTracing<? extends Request> messagingTracing() {
        return MessagingTracingImpl.build(tracing());
    }

}
