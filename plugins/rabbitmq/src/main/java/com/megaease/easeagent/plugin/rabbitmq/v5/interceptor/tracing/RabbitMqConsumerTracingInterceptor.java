/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.tracing;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqConsumerAdvice;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@AdviceTo(value = RabbitMqConsumerAdvice.class, plugin = RabbitMqPlugin.class)
public class RabbitMqConsumerTracingInterceptor implements Interceptor {
    private static final String SPAN_CONTEXT_KEY = RabbitMqConsumerTracingInterceptor.class.getName() + "-Span";

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);
        Envelope envelope = (Envelope) methodInfo.getArgs()[1];
        AMQP.BasicProperties basicProperties = (AMQP.BasicProperties) methodInfo.getArgs()[2];
        RabbitConsumerRequest consumerRequest = new RabbitConsumerRequest(envelope, basicProperties);

        Span span = context.consumerSpan(consumerRequest);
        span.kind(Span.Kind.CONSUMER);
        span.tag("rabbit.exchange", envelope.getExchange());
        span.tag("rabbit.routing_key", envelope.getRoutingKey());
        span.tag("rabbit.queue", envelope.getRoutingKey());
        if (uri != null) {
            span.tag("rabbit.broker", uri);
        }
        span.remoteServiceName("rabbitmq");
        RedirectProcessor.setTagsIfRedirected(Redirect.RABBITMQ, span);
        span.start();
        context.put(SPAN_CONTEXT_KEY, span);
        context.consumerInject(span, consumerRequest);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Span span = context.get(SPAN_CONTEXT_KEY);
        if (span == null) {
            return;
        }
        if (!methodInfo.isSuccess()) {
            span.error(methodInfo.getThrowable());
        }
        span.finish();
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }

    static class RabbitConsumerRequest implements MessagingRequest {
        private final Envelope envelope;
        private final Map<String, Object> headers = new HashMap<>();

        public RabbitConsumerRequest(Envelope envelope, AMQP.BasicProperties basicProperties) {
            this.envelope = envelope;
            Map<String, Object> originHeaders = basicProperties.getHeaders();
            if (originHeaders != null) {
                headers.putAll(originHeaders);
            }
            AgentFieldReflectAccessor.setFieldValue(basicProperties, "headers", headers);
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

        @Override
        public Span.Kind kind() {
            return Span.Kind.CONSUMER;
        }

        public String header(String key) {
            Object obj = headers.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }

        @Override
        public String name() {
            return "next-message";
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            this.headers.put(name, value);
        }
    }
}
