package com.megaease.easeagent.zipkin.rabbitmq.v5;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.messaging.ConsumerRequest;
import brave.messaging.MessagingTracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.core.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.util.Map;

public class RabbitMqConsumerTracingInterceptor implements AgentInterceptor {

    private final TraceContext.Extractor<RabbitConsumerRequest> extractor;

    public RabbitMqConsumerTracingInterceptor(Tracing tracing) {
        MessagingTracing messagingTracing = MessagingTracing.newBuilder(tracing).build();
        this.extractor = messagingTracing.propagation().extractor(RabbitConsumerRequest::header);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
//        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);

//        AgentRabbitMqConsumer consumer = (AgentRabbitMqConsumer) methodInfo.getInvoker();
//        String uri = (String) consumer.getAttach();

        String uri = null;
        if (methodInfo.getInvoker() instanceof DynamicFieldAccessor) {
            uri = (String) ((DynamicFieldAccessor) methodInfo.getInvoker()).getEaseAgent$$DynamicField$$Data();
        }

        Envelope envelope = (Envelope) methodInfo.getArgs()[1];
        AMQP.BasicProperties basicProperties = (AMQP.BasicProperties) methodInfo.getArgs()[2];
        RabbitConsumerRequest consumerRequest = new RabbitConsumerRequest(envelope, basicProperties);
        TraceContextOrSamplingFlags samplingFlags = this.extractor.extract(consumerRequest);
        Span span = Tracing.currentTracer().nextSpan(samplingFlags);
        span.name("next-message");
        span.tag("rabbit.exchange", envelope.getExchange());
        span.tag("rabbit.routing_key", envelope.getRoutingKey());
        span.tag("rabbit.queue", envelope.getRoutingKey());
        if (uri != null) {
            span.tag("rabbit.broker", uri);
        }
        span.remoteServiceName("rabbitmq");
        span.start();
        Tracer.SpanInScope spanInScope = Tracing.currentTracer().withSpanInScope(span);
        context.put(Span.class, span);
        context.put(Tracer.SpanInScope.class, spanInScope);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Tracer.SpanInScope spanInScope = ContextUtils.getFromContext(context, Tracer.SpanInScope.class);
        spanInScope.close();
        Span span = ContextUtils.getFromContext(context, Span.class);
        span.finish();
        return chain.doAfter(methodInfo, context);
    }

    static class RabbitConsumerRequest extends ConsumerRequest {

        private final Envelope envelope;
        private final AMQP.BasicProperties basicProperties;

        public RabbitConsumerRequest(Envelope envelope, AMQP.BasicProperties basicProperties) {
            this.envelope = envelope;
            this.basicProperties = basicProperties;
        }

        @Override
        public String operation() {
            return "receive";
        }

        @Override
        public String channelKind() {
            return "queue";
        }

        @Override
        public String channelName() {
            return this.envelope.getRoutingKey();
        }

        @Override
        public Object unwrap() {
            return null;
        }

        public String header(String key) {
            Map<String, Object> headers = this.basicProperties.getHeaders();
            if (headers == null) {
                return null;
            }
            Object obj = headers.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
    }
}
