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

package com.megaease.easeagent.plugin.rabbitmq.spring.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
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
import com.megaease.easeagent.plugin.rabbitmq.spring.RabbitMqMessageListenerAdvice;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;

@SuppressWarnings("unused")
@AdviceTo(RabbitMqMessageListenerAdvice.class)
public class RabbitMqOnMessageTracingInterceptor implements Interceptor {
    private static final String SCOPE_CONTEXT_KEY = RabbitMqOnMessageTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private static final String SPAN_CONTEXT_KEY = RabbitMqOnMessageTracingInterceptor.class.getName() + "-Span";

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        if (methodInfo.getArgs()[0] instanceof List) {
            Span span = context.currentTracing().nextSpan();
            span.name("on-message-list");
            context.put(SCOPE_CONTEXT_KEY, span);
            this.before4List(methodInfo, context);
        } else {
            this.before4Single(methodInfo, context);
        }
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        if (methodInfo.getArgs()[0] instanceof List) {
            this.after4List(methodInfo, context);
            Span span = context.get(SCOPE_CONTEXT_KEY);
            span.finish();
        } else {
            this.after4Single(methodInfo, context);
        }
    }

    public void after4Single(MethodInfo methodInfo, Context context) {
        this.processMessageAfter(methodInfo, context, 0);
    }

    @SuppressWarnings("unchecked")
    public void after4List(MethodInfo methodInfo, Context context) {
        List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
        for (int i = 0; i < messageList.size(); i++) {
            this.processMessageAfter(methodInfo, context, i);
        }
    }

    private void processMessageAfter(MethodInfo methodInfo, Context context, int index) {
        // CurrentTraceContext.Scope newScope = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY + index);
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY + index);
        if (!methodInfo.isSuccess()) {
            span.error(methodInfo.getThrowable());
        }
        // newScope.close();
        span.finish();
    }


    private void before4Single(MethodInfo methodInfo, Context context) {
        Message message = (Message) methodInfo.getArgs()[0];
        this.processMessageBefore(message, context, 0);
    }

    @SuppressWarnings("unchecked")
    private void before4List(MethodInfo methodInfo, Context context) {
        List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            this.processMessageBefore(message, context, i);
        }
    }

    private void processMessageBefore(Message message, Context context, int index) {
        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);
        MessageProperties messageProperties = message.getMessageProperties();
        RabbitConsumerRequest request = new RabbitConsumerRequest(message);
        // RequestContext progressContext = context.serverReceive(request);
        // TraceContextOrSamplingFlags samplingFlags = this.extractor.extract(request);
        // Span span = Tracing.currentTracer().nextSpan(samplingFlags);
        Span span = context.consumerSpan(request);
        span.tag("rabbit.exchange", messageProperties.getReceivedExchange());
        span.tag("rabbit.routing_key", messageProperties.getReceivedRoutingKey());
        span.tag("rabbit.queue", messageProperties.getConsumerQueue());
        if (uri != null) {
            span.tag("rabbit.broker", uri);
        }
        span.remoteServiceName("rabbitmq");
        span.tag(MiddlewareConstants.TYPE_TAG_NAME,  Type.RABBITMQ.getRemoteType());
        RedirectProcessor.setTagsIfRedirected(Redirect.RABBITMQ, span);
        span.start();

        // CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        // CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        // context.put(SCOPE_CONTEXT_KEY + index, newScope);
        context.put(SPAN_CONTEXT_KEY + index, span);
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }

    static class RabbitConsumerRequest implements MessagingRequest {
        private final Message message;

        public RabbitConsumerRequest(Message message) {
            this.message = message;
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
            return this.message.getMessageProperties().getConsumerQueue();
        }

        @Override
        public Object unwrap() {
            return message;
        }

        @Override
        public Span.Kind kind() {
            return Span.Kind.CONSUMER;
        }

        public String header(String name) {
            return message.getMessageProperties().getHeader(name);
        }

        @Override
        public String name() {
            return "on-message";
        }

        @Override
        public boolean cacheScope() {
            return false;
        }

        @Override
        public void setHeader(String name, String value) {
            message.getMessageProperties().setHeader(name, value);
        }
    }
}
