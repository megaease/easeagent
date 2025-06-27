/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.zipkin.impl;

import brave.propagation.CurrentTraceContext;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpContext;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.zipkin.impl.message.MessagingTracingImpl;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class TracingImpl implements ITracing {
    private static final Logger LOGGER = LoggerFactory.getLogger(TracingImpl.class);
    private final Supplier<InitializeContext> supplier;
    private final brave.Tracing tracing;
    private final brave.Tracer tracer;

    private final TraceContext.Injector<Request> defaultZipkinInjector;
    private final TraceContext.Injector<Request> clientZipkinInjector;
    private final TraceContext.Extractor<Request> defaultZipkinExtractor;

    private final MessagingTracing<MessagingRequest> messagingTracing;
    private final List<String> propagationKeys;

    private TracingImpl(@Nonnull Supplier<InitializeContext> supplier,
                        @Nonnull brave.Tracing tracing) {
        this.supplier = supplier;
        this.tracing = tracing;
        this.tracer = tracing.tracer();
        this.propagationKeys = tracing.propagation().keys();
        Propagation<String> propagation = tracing.propagation();

        this.defaultZipkinInjector = propagation.injector(Request::setHeader);
        this.clientZipkinInjector = propagation.injector(new RemoteSetterImpl<>(brave.Span.Kind.CLIENT));
        this.defaultZipkinExtractor = propagation.extractor(Request::header);
        this.messagingTracing = MessagingTracingImpl.build(tracing);
    }

    public static ITracing build(Supplier<InitializeContext> supplier, brave.Tracing tracing) {
        if (tracing == null) {
            return NoOpTracer.NO_OP_TRACING;
        }

        return new TracingImpl(supplier, tracing);
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public boolean hasCurrentSpan() {
        return tracing().currentTraceContext().get() != null;
    }


    private brave.Tracer tracer() {
        return this.tracer;
    }

    private brave.Tracing tracing() {
        return this.tracing;
    }

    @Override
    public Span currentSpan() {
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
        return SpanImpl.build(tracing(), bSpan, cacheScope, defaultZipkinInjector);
    }

    private void setInfo(brave.Span span, Request request) {
        Span.Kind kind = request.kind();
        if (kind != null) {
            span.kind(SpanImpl.braveKind(kind));
        }
        span.name(request.name());
    }

    private TraceContext currentTraceContext() {
        if (tracer == null) {
            LOGGER.debug("tracer was null.");
            return null;
        }
        brave.Span span = tracer.currentSpan();
        if (span == null) {
            return null;
        }
        return span.context();
    }

    @Override
    public SpanContext exportAsync() {
        TraceContext traceContext = currentTraceContext();
        if (traceContext == null) {
            return NoOpTracer.NO_OP_SPAN_CONTEXT;
        }
        return new SpanContextImpl(traceContext);
    }

    @Override
    public Scope importAsync(SpanContext snapshot) {
        if (snapshot.isNoop()) {
            return NoOpTracer.NO_OP_SCOPE;
        }
        Object context = snapshot.unwrap();
        if (context instanceof TraceContext) {
            TraceContext traceContext = (TraceContext) context;
            CurrentTraceContext.Scope scope = tracing().currentTraceContext().maybeScope(traceContext);
            return new ScopeImpl(scope);
        } else {
            LOGGER.warn("import async span to brave.Tracing fail: SpanContext.unwrap() result Class<{}> must be Class<{}>", context.getClass().getName(), TraceContext.class.getName());
        }
        return NoOpTracer.NO_OP_SCOPE;
    }

    @Override
    public RequestContext clientRequest(Request request) {
        brave.Span span = SpanImpl.nextBraveSpan(tracing, defaultZipkinExtractor, request);
        AsyncRequest asyncRequest = new AsyncRequest(request);
        clientZipkinInjector.inject(span.context(), asyncRequest);
        Span newSpan = build(span, request.cacheScope());
        return new RequestContextImpl(newSpan, newSpan.maybeScope(), asyncRequest);
    }

    @Override
    public RequestContext serverReceive(Request request) {
        TraceContext maybeParent = tracing.currentTraceContext().get();
        // Unlike message consumers, we try current span before trying extraction. This is the proper
        // order because the span in scope should take precedence over a potentially stale header entry.
        //
        brave.Span span;
        if (maybeParent == null) {
            TraceContextOrSamplingFlags extracted = defaultZipkinExtractor.extract(request);
            span = extracted.context() != null
                ? tracer().joinSpan(extracted.context())
                : tracer().nextSpan(extracted);
        } else { // If we have a span in scope assume headers were cleared before
            span = tracing.tracer().newChild(maybeParent);
        }

        setInfo(span, request);
        AsyncRequest asyncRequest = new AsyncRequest(request);
        defaultZipkinInjector.inject(span.context(), asyncRequest);
        Span newSpan = build(span, request.cacheScope());
        return new RequestContextImpl(newSpan, newSpan.maybeScope(), asyncRequest);
    }

    @Override
    public List<String> propagationKeys() {
        return propagationKeys;
    }

    @Override
    public Span nextSpan() {
        return build(tracer().nextSpan(), false);
    }

    @Override
    public MessagingTracing<MessagingRequest> messagingTracing() {
        return messagingTracing;
    }

    @Override
    public Object unwrap() {
        return tracing;
    }

    @Override
    public Span consumerSpan(MessagingRequest request) {
        return this.messagingTracing.consumerSpan(request);
    }

    @Override
    public Span producerSpan(MessagingRequest request) {
        return this.messagingTracing.producerSpan(request);
    }
}
