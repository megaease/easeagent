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

package com.megaease.easeagent.zipkin;

import brave.Tracing;
import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.rabbitmq.v5.RabbitMqConsumerTracingInterceptor;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class RabbitMqConsumerTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Config config = this.createConfig(RabbitMqConsumerTracingInterceptor.ENABLE_KEY, "true");
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracing tracing = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        spanInfoMap.putAll(tmpMap);
                        return super.end(context, span, cause);
                    }
                })
                .build();

        RabbitMqConsumerTracingInterceptor interceptor = new RabbitMqConsumerTracingInterceptor(tracing, config);

        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.MQ_URI, "mock-uri");
        Consumer consumer = mock(Consumer.class);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        Map<String, Object> headers = new HashMap<>();
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties(null, null, headers, 0, 0, null, null, null, null, new Date(), null, null, null, null);
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(consumer)
                .method("handleDelivery")
                .args(new Object[]{"", envelope, basicProperties, new byte[0]})
                .build();

        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("rabbit.exchange", "exchange");
        expectedMap.put("rabbit.routing_key", "routingKey");
        expectedMap.put("rabbit.broker", "mock-uri");
        expectedMap.put("rabbit.queue", "routingKey");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void disableTracing() {
        Config config = this.createConfig(RabbitMqConsumerTracingInterceptor.ENABLE_KEY, "false");
        Map<String, String> spanInfoMap = new HashMap<>();
        Tracing tracing = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .addSpanHandler(new SpanHandler() {
                    @Override
                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
                        Map<String, String> tmpMap = new HashMap<>(span.tags());
                        spanInfoMap.putAll(tmpMap);
                        return super.end(context, span, cause);
                    }
                })
                .build();

        RabbitMqConsumerTracingInterceptor interceptor = new RabbitMqConsumerTracingInterceptor(tracing, config);

        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.MQ_URI, "mock-uri");
        Consumer consumer = mock(Consumer.class);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        Map<String, Object> headers = new HashMap<>();
        AMQP.BasicProperties basicProperties = new AMQP.BasicProperties(null, null, headers, 0, 0, null, null, null, null, new Date(), null, null, null, null);
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(consumer)
                .method("handleDelivery")
                .args(new Object[]{"", envelope, basicProperties, new byte[0]})
                .build();

        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Assert.assertTrue(spanInfoMap.isEmpty());
    }
}
