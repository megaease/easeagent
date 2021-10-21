package com.megaease.easeagent.plugin.tracer;

import brave.messaging.ConsumerRequest;
import brave.messaging.MessagingRequest;
import brave.messaging.ProducerRequest;
import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

import javax.annotation.Nonnull;
import java.util.function.Function;

public class ZipkinMessagingTracing<R extends Request> implements MessagingTracing {
    private final brave.messaging.MessagingTracing messagingTracing;

    private ZipkinMessagingTracing(brave.messaging.MessagingTracing messagingTracing) {
        this.messagingTracing = messagingTracing;
    }

    public static MessagingTracing<? extends Request> build(brave.Tracing tracing) {
        if (tracing == null) {
            return NoOpTracer.NO_OP_MESSAGING_TRACING;
        }
        brave.messaging.MessagingTracing messagingTracing = brave.messaging.MessagingTracing.newBuilder(tracing).build();
        return new ZipkinMessagingTracing(messagingTracing);
    }

    @Override
    public Extractor<R> extractor() {
        return new ZipkinExtractor(messagingTracing.propagation().extractor(Request::header));
    }

    @Override
    public Injector<R> injector() {
        return new ZipkinInjector(messagingTracing.propagation().injector(Request::setHeader));
    }

    @Override
    public Function<R, Boolean> consumerSampler() {
        return new SamplerFunction(ZipkinConsumerRequest.BUILDER,  messagingTracing.consumerSampler());
    }

    @Override
    public Function<R, Boolean> producerSampler() {
        return new SamplerFunction(ZipkinProducerRequest.BUILDER,  messagingTracing.producerSampler());
    }

    @Override
    public boolean consumerSampler(Request request) {
        return messagingTracing.consumerSampler().trySample(new ZipkinConsumerRequest(request));
    }

    @Override
    public boolean producerSampler(Request request) {
        return messagingTracing.producerSampler().trySample(new ZipkinProducerRequest(request));
    }


    public class ZipkinExtractor implements Extractor {
        private final TraceContext.Extractor<Request> extractor;

        public ZipkinExtractor(TraceContext.Extractor<Request> extractor) {
            this.extractor = extractor;
        }

        @Override
        public Object extract(Request request) {
            return extractor.extract(request);
        }
    }

    public class ZipkinInjector implements Injector {
        private final TraceContext.Injector<Request> injector;

        public ZipkinInjector(TraceContext.Injector<Request> injector) {
            this.injector = injector;
        }

        @Override
        public void inject(Span span, Request request) {
            if (span instanceof ZipkinSpan) {
                this.injector.inject(((ZipkinSpan) span).getSpan().context(), request);
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
        public static Function<Request, MessagingRequest> BUILDER = request -> new ZipkinProducerRequest(request);
        private final Request request;

        public ZipkinProducerRequest(Request request) {
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
        public static Function<Request, MessagingRequest> BUILDER = request -> new ZipkinConsumerRequest(request);
        private final Request request;

        public ZipkinConsumerRequest(Request request) {
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
