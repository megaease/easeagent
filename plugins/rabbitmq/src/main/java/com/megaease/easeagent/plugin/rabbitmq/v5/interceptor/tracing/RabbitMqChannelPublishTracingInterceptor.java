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

import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.MessagingRequest;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqChannelAdvice;
import com.rabbitmq.client.AMQP;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@AdviceTo(value = RabbitMqChannelAdvice.class, qualifier = "basicPublish", plugin = RabbitMqPlugin.class)
public class RabbitMqChannelPublishTracingInterceptor implements Interceptor {
    private static final String SPAN_CONTEXT_KEY = RabbitMqChannelPublishTracingInterceptor.class.getName() + "-Span";

    @Override
    public void before(MethodInfo methodInfo, Context context) {
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

        Span span = context.producerSpan(producerRequest);
        span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.RABBITMQ.getRemoteType());
        RedirectProcessor.setTagsIfRedirected(Redirect.RABBITMQ, span);
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
    }

    @Override
    public void after(MethodInfo method, Context context) {
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
        if (span == null) {
            return;
        }
        if (!method.isSuccess()) {
            span.error(method.getThrowable());
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

    static class RabbitProducerRequest implements MessagingRequest {
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
            AgentFieldReflectAccessor.setFieldValue(this.basicProperties, "headers", headers);
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
            if (this.exchange != null && this.exchange.length() > 0) {
                return this.exchange;
            }
            return this.routingKey;
        }

        @Override
        public Object unwrap() {
            return null;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.PRODUCER;
        }

        @Override
        public String header(String key) {
            Map<String, Object> headers = this.basicProperties.getHeaders();
            Object obj = headers.get(key);
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }

        @Override
        public String name() {
            return "publish";
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String key, String value) {
            this.basicProperties.getHeaders().put(key, value);
        }
    }
}
