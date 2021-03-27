package com.megaease.easeagent.zipkin.rabbitmq.v5;

import brave.Span;
import brave.Tracing;
import brave.messaging.MessagingTracing;
import brave.messaging.ProducerRequest;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.rabbitmq.client.AMQP;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

public class RabbitMqProducerTracingInterceptor implements AgentInterceptor {

    private final TraceContext.Injector<RabbitProducerRequest> injector;
    private static final String SPAN_CONTEXT_KEY = RabbitMqProducerTracingInterceptor.class.getName() + "-Span";

    public RabbitMqProducerTracingInterceptor(Tracing tracing) {
        MessagingTracing messagingTracing = MessagingTracing.newBuilder(tracing).build();
        this.injector = messagingTracing.propagation().injector(RabbitProducerRequest::addHeader);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);
        String exchange = null;
        if (methodInfo.getArgs()[0] != null) {
            exchange = (String) methodInfo.getArgs()[0];
        }
        String routingKey = null;
        if (methodInfo.getArgs()[1] != null) {
            routingKey = (String) methodInfo.getArgs()[1];
        }
        AMQP.BasicProperties basicProperties = (AMQP.BasicProperties) methodInfo.getArgs()[4];
        RabbitProducerRequest producerRequest = new RabbitProducerRequest(exchange, routingKey, basicProperties);
        context.put(RabbitProducerRequest.class, producerRequest);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        TraceContext traceContext = currentTraceContext.get();
        Span span;
        if (traceContext == null) {
            span = Tracing.currentTracer().nextSpan();
        } else {
            span = Tracing.currentTracer().newChild(traceContext);
        }
        span.kind(Span.Kind.PRODUCER);
        span.name("publish");
        if (exchange != null) {
            span.tag("rabbit.exchange", exchange);
        }
        if (routingKey != null) {
            span.tag("rabbit.routing_key", routingKey);
        }
        span.tag("rabbit.broker", uri);
        if (!span.isNoop()) {
            span.remoteServiceName("rabbitmq");
            span.start();
        }
        context.put(SPAN_CONTEXT_KEY, span);
        injector.inject(span.context(), producerRequest);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
        span.finish();
        return chain.doAfter(methodInfo, context);
    }

    static class RabbitProducerRequest extends ProducerRequest {

        private final String exchange;

        private final String routingKey;

        private final AMQP.BasicProperties basicProperties;

        public RabbitProducerRequest(String exchange, String routingKey, AMQP.BasicProperties basicProperties) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.basicProperties = basicProperties;

            Map<String, Object> headers = new HashMap<>();
            if (this.basicProperties.getHeaders() != null) {
                headers.putAll(this.basicProperties.getHeaders());
            }
            AgentFieldAccessor.setFieldValue(this.basicProperties, "headers", headers);
        }

        @Override
        public String operation() {
            return "send";
        }

        @Override
        public String channelKind() {
            return "queue";
        }

        @Override
        public String channelName() {
            if (StringUtils.isNotEmpty(this.exchange)) {
                return this.exchange;
            }
            return this.routingKey;
        }

        @Override
        public Object unwrap() {
            return null;
        }

        public String header(String key) {
            Map<String, Object> headers = this.basicProperties.getHeaders();
            Object obj = headers.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }

        public void addHeader(String key, String value) {
            this.basicProperties.getHeaders().put(key, value);
        }
    }

}
