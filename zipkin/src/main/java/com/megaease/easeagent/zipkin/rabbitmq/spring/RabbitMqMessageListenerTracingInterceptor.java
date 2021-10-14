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

package com.megaease.easeagent.zipkin.rabbitmq.spring;

import brave.Span;
import brave.Tracing;
import brave.messaging.ConsumerRequest;
import brave.messaging.MessagingTracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;
import brave.propagation.TraceContextOrSamplingFlags;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;
import java.util.Map;

public class RabbitMqMessageListenerTracingInterceptor implements AgentInterceptor {
    public static final String ENABLE_KEY = "observability.tracings.rabbit.enabled";
    private final TraceContext.Extractor<RabbitConsumerRequest> extractor;
    private final Config config;
    private static final String SCOPE_CONTEXT_KEY = RabbitMqMessageListenerTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private static final String SPAN_CONTEXT_KEY = RabbitMqMessageListenerTracingInterceptor.class.getName() + "-Span";
    private static final String TRACING_DISABLE = RabbitMqMessageListenerTracingInterceptor.class.getName() + "-Tracing.disable";

    public RabbitMqMessageListenerTracingInterceptor(Tracing tracing, Config config) {
        MessagingTracing messagingTracing = MessagingTracing.newBuilder(tracing).build();
        this.extractor = messagingTracing.propagation().extractor(RabbitConsumerRequest::header);
        this.config = config;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            context.put(TRACING_DISABLE, true);
            chain.doBefore(methodInfo, context);
            return;
        }
        if (methodInfo.getArgs()[0] instanceof List) {
            this.before4List(methodInfo, context);
        } else {
            this.before4Single(methodInfo, context);
        }
        chain.doBefore(methodInfo, context);
    }

    private void before4Single(MethodInfo methodInfo, Map<Object, Object> context) {
        Message message = (Message) methodInfo.getArgs()[0];
        this.processMessageBefore(message, context, 0);
    }

    @SuppressWarnings("unchecked")
    private void before4List(MethodInfo methodInfo, Map<Object, Object> context) {
        List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
        for (int i = 0; i < messageList.size(); i++) {
            Message message = messageList.get(i);
            this.processMessageBefore(message, context, i);
        }
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (ContextUtils.getFromContext(context, TRACING_DISABLE) != null) {
            return chain.doAfter(methodInfo, context);
        }
        if (methodInfo.getArgs()[0] instanceof List) {
            this.after4List(methodInfo, context);
        } else {
            this.after4Single(methodInfo, context);
        }
        return chain.doAfter(methodInfo, context);
    }

    public void after4Single(MethodInfo methodInfo, Map<Object, Object> context) {
        this.processMessageAfter(methodInfo, context, 0);
    }

    @SuppressWarnings("unchecked")
    public void after4List(MethodInfo methodInfo, Map<Object, Object> context) {
        List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
        for (int i = 0; i < messageList.size(); i++) {
            this.processMessageAfter(methodInfo, context, i);
        }
    }

    private void processMessageBefore(Message message, Map<Object, Object> context, int index) {
        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);
        MessageProperties messageProperties = message.getMessageProperties();
        RabbitConsumerRequest request = new RabbitConsumerRequest(message);
        TraceContextOrSamplingFlags samplingFlags = this.extractor.extract(request);
        Span span = Tracing.currentTracer().nextSpan(samplingFlags);
        span.kind(Span.Kind.CLIENT);
        span.name("on-message");
        span.tag("rabbit.exchange", messageProperties.getReceivedExchange());
        span.tag("rabbit.routing_key", messageProperties.getReceivedRoutingKey());
        span.tag("rabbit.queue", messageProperties.getConsumerQueue());
        if (uri != null) {
            span.tag("rabbit.broker", uri);
        }
        span.remoteServiceName("rabbitmq");
        span.start();

        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        context.put(SCOPE_CONTEXT_KEY + index, newScope);
        context.put(SPAN_CONTEXT_KEY + index, span);
    }

    private void processMessageAfter(MethodInfo methodInfo, Map<Object, Object> context, int index) {
        CurrentTraceContext.Scope newScope = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY + index);
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY + index);
        if (!methodInfo.isSuccess()) {
            span.error(methodInfo.getThrowable());
        }
        newScope.close();
        span.finish();
    }

    static class RabbitConsumerRequest extends ConsumerRequest {

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

        public String header(String name) {
            return message.getMessageProperties().getHeader(name);
        }
    }
}
