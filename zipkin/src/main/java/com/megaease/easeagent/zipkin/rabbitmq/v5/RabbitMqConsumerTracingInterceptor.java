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

package com.megaease.easeagent.zipkin.rabbitmq.v5;

import brave.Span;
import brave.Tracing;
import brave.messaging.ConsumerRequest;
import brave.messaging.MessagingTracing;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.util.HashMap;
import java.util.Map;

public class RabbitMqConsumerTracingInterceptor implements AgentInterceptor {

    private static final String SPAN_CONTEXT_KEY = RabbitMqConsumerTracingInterceptor.class.getName() + "-Span";

    private final TraceContext.Extractor<RabbitConsumerRequest> extractor;
    private final TraceContext.Injector<RabbitConsumerRequest> injector;

    public RabbitMqConsumerTracingInterceptor(Tracing tracing) {
        MessagingTracing messagingTracing = MessagingTracing.newBuilder(tracing).build();
        this.extractor = messagingTracing.propagation().extractor(RabbitConsumerRequest::header);
        this.injector = messagingTracing.propagation().injector(RabbitConsumerRequest::addHeader);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);
        Envelope envelope = (Envelope) methodInfo.getArgs()[1];
        AMQP.BasicProperties basicProperties = (AMQP.BasicProperties) methodInfo.getArgs()[2];
        RabbitConsumerRequest consumerRequest = new RabbitConsumerRequest(envelope, basicProperties);
        TraceContextOrSamplingFlags samplingFlags = this.extractor.extract(consumerRequest);
        Span span = Tracing.currentTracer().nextSpan(samplingFlags);
        span.kind(Span.Kind.CONSUMER);
        span.name("next-message");
        span.tag("rabbit.exchange", envelope.getExchange());
        span.tag("rabbit.routing_key", envelope.getRoutingKey());
        span.tag("rabbit.queue", envelope.getRoutingKey());
        if (uri != null) {
            span.tag("rabbit.broker", uri);
        }
        span.remoteServiceName("rabbitmq");
        span.start();
        context.put(SPAN_CONTEXT_KEY, span);
        this.injector.inject(span.context(), consumerRequest);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
        if (!methodInfo.isSuccess()) {
            span.error(methodInfo.getThrowable());
        }
        span.finish();
        return chain.doAfter(methodInfo, context);
    }

    static class RabbitConsumerRequest extends ConsumerRequest {

        private final Envelope envelope;
        private final Map<String, Object> headers = new HashMap<>();

        public RabbitConsumerRequest(Envelope envelope, AMQP.BasicProperties basicProperties) {
            this.envelope = envelope;
            Map<String, Object> originHeaders = basicProperties.getHeaders();
            if (originHeaders != null) {
                headers.putAll(originHeaders);
            }
            AgentFieldAccessor.setFieldValue(basicProperties, "headers", headers);
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
            Object obj = headers.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }

        public void addHeader(String key, Object value) {
            this.headers.put(key, value);
        }
    }
}
