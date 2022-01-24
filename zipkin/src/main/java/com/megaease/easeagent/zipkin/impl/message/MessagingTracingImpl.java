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

package com.megaease.easeagent.zipkin.impl.message;

import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.zipkin.impl.MessageImpl;
import com.megaease.easeagent.zipkin.impl.RemoteGetterImpl;
import com.megaease.easeagent.zipkin.impl.RemoteSetterImpl;
import com.megaease.easeagent.zipkin.impl.SpanImpl;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class MessagingTracingImpl<R extends MessagingRequest> implements MessagingTracing<R> {
    private final brave.messaging.MessagingTracing messagingTracing;
    private final Extractor<R> producerExtractor;
    private final Extractor<R> consumerExtractor;
    private final Injector<R> producerInjector;
    private final Injector<R> consumerInjector;

    private final Function<R, Boolean> consumerSampler;
    private final Function<R, Boolean> producerSampler;

    private final TraceContext.Injector<R> zipkinProducerInjector;
    private final TraceContext.Injector<R> zipkinConsumerInjector;
    private final TraceContext.Extractor<R> zipkinProducerExtractor;
    private final TraceContext.Extractor<R> zipkinConsumerExtractor;

    private MessagingTracingImpl(brave.messaging.MessagingTracing messagingTracing) {
        this.messagingTracing = messagingTracing;
        this.zipkinProducerExtractor = messagingTracing.propagation().extractor(new RemoteGetterImpl<>(brave.Span.Kind.PRODUCER));
        this.zipkinConsumerExtractor = messagingTracing.propagation().extractor(new RemoteGetterImpl<>(brave.Span.Kind.CONSUMER));
        this.zipkinProducerInjector = messagingTracing.propagation().injector(new RemoteSetterImpl<>(brave.Span.Kind.PRODUCER));
        this.zipkinConsumerInjector = messagingTracing.propagation().injector(new RemoteSetterImpl<>(brave.Span.Kind.CONSUMER));

        this.producerExtractor = new ExtractorImpl(this.zipkinProducerExtractor);
        this.consumerExtractor = new ExtractorImpl(this.zipkinConsumerExtractor);
        this.producerInjector = new InjectorImpl(this.zipkinProducerInjector);
        this.consumerInjector = new InjectorImpl(this.zipkinConsumerInjector);

        this.consumerSampler = new SamplerFunction(ZipkinConsumerRequest::new, messagingTracing.consumerSampler());
        this.producerSampler = new SamplerFunction(ZipkinProducerRequest::new, messagingTracing.producerSampler());
    }

    public static MessagingTracing<MessagingRequest> build(brave.Tracing tracing) {
        if (tracing == null) {
            return NoOpTracer.NO_OP_MESSAGING_TRACING;
        }
        brave.messaging.MessagingTracing messagingTracing = brave.messaging.MessagingTracing
            .newBuilder(tracing).build();

        return new MessagingTracingImpl<>(messagingTracing);
    }

    @Override
    public Span consumerSpan(MessagingRequest request) {
        brave.Tracing tracing = messagingTracing.tracing();
        brave.Span span = SpanImpl.nextBraveSpan(tracing, this.zipkinConsumerExtractor, request);
        if (span.isNoop()) {
            return NoOpTracer.NO_OP_SPAN;
        }
        setMessageInfo(span, request);
        Span eSpan = SpanImpl.build(messagingTracing.tracing(), span,
            request.cacheScope(), this.zipkinConsumerInjector);

        return NoOpTracer.noNullSpan(eSpan);
    }

    @Override
    public Span producerSpan(MessagingRequest request) {
        brave.Tracing tracing = messagingTracing.tracing();
        brave.Span span = SpanImpl.nextBraveSpan(tracing, this.zipkinProducerExtractor, request);
        if (span.isNoop()) {
            return NoOpTracer.NO_OP_SPAN;
        }
        setMessageInfo(span, request);
        Span eSpan = SpanImpl.build(messagingTracing.tracing(), span, request.cacheScope(), zipkinProducerInjector);
        producerInjector.inject(eSpan, (R) request);
        return NoOpTracer.noNullSpan(eSpan);
    }

    @Override
    public Extractor<R> producerExtractor() {
        return producerExtractor;
    }

    @Override
    public Extractor<R> consumerExtractor() {
        return consumerExtractor;
    }

    @Override
    public Injector<R> producerInjector() {
        return producerInjector;
    }

    @Override
    public Injector<R> consumerInjector() {
        return consumerInjector;
    }

    @Override
    public Function<R, Boolean> consumerSampler() {
        return consumerSampler;
    }

    @Override
    public Function<R, Boolean> producerSampler() {
        return producerSampler;
    }

    private void setMessageInfo(brave.Span span, MessagingRequest request) {
        if (request.operation() != null) {
            span.tag("messaging.operation", request.operation());
        }
        if (request.channelKind() != null) {
            span.tag("messaging.channel_kind", request.channelKind());
        }
        if (request.channelName() != null) {
            span.tag("messaging.channel_name", request.channelName());
        }
    }

    public class ExtractorImpl implements Extractor<R> {
        private final TraceContext.Extractor<R> extractor;

        public ExtractorImpl(TraceContext.Extractor<R> extractor) {
            this.extractor = extractor;
        }

        @Override
        public Message<TraceContextOrSamplingFlags> extract(R request) {
            return new MessageImpl(extractor.extract(request));
        }
    }

    public class InjectorImpl implements Injector<R> {
        private final TraceContext.Injector<R> injector;

        public InjectorImpl(TraceContext.Injector<R> injector) {
            this.injector = injector;
        }

        @Override
        public void inject(Span span, R request) {
            Object spanO = span.unwrap();
            if (spanO instanceof brave.Span) {
                this.injector.inject(((brave.Span) spanO).context(), request);
            }
        }

        public TraceContext.Injector<R> getInjector() {
            return this.injector;
        }
    }

    public class SamplerFunction implements Function<R, Boolean> {
        private final Function<R, brave.messaging.MessagingRequest> builder;
        private final brave.sampler.SamplerFunction<brave.messaging.MessagingRequest> function;

        public SamplerFunction(@Nonnull Function<R, brave.messaging.MessagingRequest> builder,
                               @Nonnull brave.sampler.SamplerFunction<brave.messaging.MessagingRequest> function) {
            this.builder = builder;
            this.function = function;
        }

        @Override
        public Boolean apply(R request) {
            return function.trySample(builder.apply(request));
        }
    }

}
