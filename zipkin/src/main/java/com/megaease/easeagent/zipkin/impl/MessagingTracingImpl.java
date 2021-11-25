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

import brave.messaging.ConsumerRequest;
import brave.messaging.MessagingRequest;
import brave.messaging.ProducerRequest;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class MessagingTracingImpl<R extends com.megaease.easeagent.plugin.api.trace.MessagingRequest> implements MessagingTracing {
    private final brave.messaging.MessagingTracing messagingTracing;
    private final Extractor<R> extractor;
    private final Injector<R> producerInjector;
    private final Injector<R> consumerInjector;
    private final Function<R, Boolean> consumerSampler;
    private final Function<R, Boolean> producerSampler;

    private MessagingTracingImpl(brave.messaging.MessagingTracing messagingTracing) {
        this.messagingTracing = messagingTracing;
        this.extractor = new ExtractorImpl(messagingTracing.propagation().extractor(com.megaease.easeagent.plugin.api.trace.MessagingRequest::header));
        this.producerInjector = new InjectorImpl(messagingTracing.propagation().injector(new RemoteSetterImpl(brave.Span.Kind.PRODUCER)));
        this.consumerInjector = new InjectorImpl(messagingTracing.propagation().injector(new RemoteSetterImpl(brave.Span.Kind.CONSUMER)));
        this.consumerSampler = new SamplerFunction(ZipkinConsumerRequest.BUILDER, messagingTracing.consumerSampler());
        this.producerSampler = new SamplerFunction(ZipkinProducerRequest.BUILDER, messagingTracing.producerSampler());
    }

    public static MessagingTracing<? extends Request> build(brave.Tracing tracing) {
        if (tracing == null) {
            return NoOpTracer.NO_OP_MESSAGING_TRACING;
        }
        brave.messaging.MessagingTracing messagingTracing = brave.messaging.MessagingTracing.newBuilder(tracing).build();
        return new MessagingTracingImpl(messagingTracing);
    }

    @Override
    public Extractor<R> extractor() {
        return extractor;
    }

    @Override
    public Injector<R> producerInjector() {
        return producerInjector;
    }

    @Override
    public Injector consumerInjector() {
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

    @Override
    public boolean consumerSampler(com.megaease.easeagent.plugin.api.trace.MessagingRequest request) {
        return messagingTracing.consumerSampler().trySample(new ZipkinConsumerRequest(request));
    }

    @Override
    public boolean producerSampler(com.megaease.easeagent.plugin.api.trace.MessagingRequest request) {
        return messagingTracing.producerSampler().trySample(new ZipkinProducerRequest(request));
    }


    public class ExtractorImpl implements Extractor {
        private final TraceContext.Extractor<com.megaease.easeagent.plugin.api.trace.MessagingRequest> extractor;

        public ExtractorImpl(TraceContext.Extractor<com.megaease.easeagent.plugin.api.trace.MessagingRequest> extractor) {
            this.extractor = extractor;
        }

        @Override
        public Message<TraceContextOrSamplingFlags> extract(com.megaease.easeagent.plugin.api.trace.MessagingRequest request) {
            return new MessageImpl(extractor.extract(request));
        }
    }

    public class InjectorImpl implements Injector {
        private final TraceContext.Injector<com.megaease.easeagent.plugin.api.trace.MessagingRequest> injector;

        public InjectorImpl(TraceContext.Injector<com.megaease.easeagent.plugin.api.trace.MessagingRequest> injector) {
            this.injector = injector;
        }

        @Override
        public void inject(Span span, com.megaease.easeagent.plugin.api.trace.MessagingRequest request) {
            if (span instanceof SpanImpl) {
                this.injector.inject(((SpanImpl) span).getSpan().context(), request);
            }
        }
    }

    public static class SamplerFunction<R extends Request> implements Function<R, Boolean> {
        private final Function<Request, MessagingRequest> builder;
        private final brave.sampler.SamplerFunction<MessagingRequest> function;

        public SamplerFunction(@Nonnull Function<Request, MessagingRequest> builder, @Nonnull brave.sampler.SamplerFunction<MessagingRequest> function) {
            this.builder = builder;
            this.function = function;
        }

        @Override
        public Boolean apply(Request request) {
            return function.trySample(builder.apply(request));
        }
    }

    public static class ZipkinProducerRequest extends ProducerRequest {
        public static Function<com.megaease.easeagent.plugin.api.trace.MessagingRequest, MessagingRequest> BUILDER = request -> new ZipkinProducerRequest(request);
        private final com.megaease.easeagent.plugin.api.trace.MessagingRequest request;

        public ZipkinProducerRequest(com.megaease.easeagent.plugin.api.trace.MessagingRequest request) {
            this.request = request;
        }

        @Override
        public String operation() {
            return request.operation();
        }

        @Override
        public String channelKind() {
            return request.channelKind();
        }

        @Override
        public String channelName() {
            return request.channelName();
        }

        @Override
        public Object unwrap() {
            return request.unwrap();
        }
    }

    public static class ZipkinConsumerRequest extends ConsumerRequest {
        public static Function<com.megaease.easeagent.plugin.api.trace.MessagingRequest, MessagingRequest> BUILDER = request -> new ZipkinConsumerRequest(request);
        private final com.megaease.easeagent.plugin.api.trace.MessagingRequest request;

        public ZipkinConsumerRequest(com.megaease.easeagent.plugin.api.trace.MessagingRequest request) {
            this.request = request;
        }

        @Override
        public String operation() {
            return request.operation();
        }

        @Override
        public String channelKind() {
            return request.channelKind();
        }

        @Override
        public String channelName() {
            return request.channelName();
        }

        @Override
        public Object unwrap() {
            return request.unwrap();
        }
    }
}