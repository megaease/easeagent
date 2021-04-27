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

package com.megaease.easeagent.zipkin.kafka.spring;

import brave.Span;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.kafka.brave.KafkaTracing;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class KafkaMessageListenerTracingInterceptor implements AgentInterceptor {

    private final KafkaTracing kafkaTracing;

    private final String SCOPE_CONTEXT_KEY = KafkaMessageListenerTracingInterceptor.class.getName() + "-CurrentTraceContext.Scope";
    private final String SPAN_CONTEXT_KEY = KafkaMessageListenerTracingInterceptor.class.getName() + "-Span";

    public KafkaMessageListenerTracingInterceptor(Tracing tracing) {
        this.kafkaTracing = KafkaTracing.newBuilder(tracing).remoteServiceName("kafka").build();
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        String uri = ContextUtils.getFromContext(context, ContextCons.MQ_URI);
        Span span = this.kafkaTracing.nextSpan(consumerRecord).name("on-message")
                .kind(Span.Kind.CLIENT)
                .remoteServiceName("kafka")
                .tag("kafka.broker", uri)
                .start();

        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        context.put(SCOPE_CONTEXT_KEY, newScope);
        context.put(SPAN_CONTEXT_KEY, span);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        CurrentTraceContext.Scope newScope = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY);
        Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
        if (!methodInfo.isSuccess()) {
            span.error(methodInfo.getThrowable());
        }
        newScope.close();
        span.finish();
        return chain.doAfter(methodInfo, context);
    }
}
