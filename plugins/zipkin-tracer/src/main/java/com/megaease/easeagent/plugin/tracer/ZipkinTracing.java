package com.megaease.easeagent.plugin.tracer;

import brave.Tracer;
import brave.propagation.ThreadLocalSpan;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.MessagingTracing;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.Tracing;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nonnull;

public class ZipkinTracing implements Tracing {
    private final brave.Tracing tracing;
    private final brave.Tracer tracer;
    private final TraceContext.Injector<AsyncRequest> defaultInjector;

    public ZipkinTracing(@Nonnull Tracer tracer, @Nonnull brave.Tracing tracing, @Nonnull TraceContext.Injector<AsyncRequest> defaultInjector) {
        this.defaultInjector = defaultInjector;
        this.tracer = tracer;
        this.tracing = tracing;
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
            span = ZipkinSpan.build(tracer.currentSpan());
        }
        return NoOpTracer.noNullSpan(span);
    }

    public AsyncContext exportAsync(Request request) {
        AsyncRequest asyncRequest = new AsyncRequest(request);
        brave.Span span = tracer().nextSpan();
        defaultInjector.inject(span.context(), asyncRequest);
        return new AsyncContextImpl(this, span, asyncRequest);
    }

    @Override
    public void importAsync(AsyncContext snapshot) {
        if (snapshot instanceof AsyncContextImpl) {
            ((AsyncContextImpl) snapshot).getSpan().start();
        }
    }

    @Override
    public ProgressContext nextProgress(Request request) {
        return null;
    }

    @Override
    public void importProgress(Request request) {

    }

    @Override
    public Span nextSpan(Object message) {
        Span span = null;
        if (message == null) {
            span = ZipkinSpan.build(ThreadLocalSpan.CURRENT_TRACER.next());
        } else if (message instanceof TraceContextOrSamplingFlags) {
            span = ZipkinSpan.build(ThreadLocalSpan.CURRENT_TRACER.next((TraceContextOrSamplingFlags) message));
        }
        return NoOpTracer.noNullSpan(span);
    }

    @Override
    public MessagingTracing<? extends Request> messagingTracing() {
        return ZipkinMessagingTracing.build(tracing());
    }

}
