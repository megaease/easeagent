///*
// * Copyright (c) 2017, MegaEase
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.megaease.easeagent.zipkin;
//
//import brave.Tracing;
//import brave.handler.MutableSpan;
//import brave.handler.SpanHandler;
//import brave.propagation.TraceContext;
//import com.megaease.easeagent.plugin.api.context.ContextCons;
//import com.megaease.easeagent.config.Config;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
//import com.megaease.easeagent.plugin.MethodInfo;
//import com.megaease.easeagent.core.utils.ContextUtils;
//import com.megaease.easeagent.zipkin.rabbitmq.spring.RabbitMqMessageListenerTracingInterceptor;
//import org.junit.Assert;
//import org.junit.Test;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageProperties;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.Mockito.mock;
//
//public class RabbitMqMessageListenerTracingInterceptorTest extends BaseZipkinTest {
//
//    @Test
//    public void success() {
//        Config config = this.createConfig(RabbitMqMessageListenerTracingInterceptor.ENABLE_KEY, "true");
//        Map<String, String> spanInfoMap = new HashMap<>();
//        Tracing tracing = Tracing.newBuilder()
//                .currentTraceContext(currentTraceContext)
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        Map<String, String> tmpMap = new HashMap<>(span.tags());
//                        spanInfoMap.putAll(tmpMap);
//                        return super.end(context, span, cause);
//                    }
//                }).build();
//
//
//        Map<Object, Object> context = ContextUtils.createContext();
//        context.put(ContextCons.MQ_URI, "mock-uri");
//
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setConsumerQueue("mock-queue");
//        messageProperties.setReceivedExchange("mock-exchange");
//        messageProperties.setReceivedRoutingKey("mock-routingKey");
//
//        Message message = new Message(new byte[0], messageProperties);
//        MethodInfo methodInfo = MethodInfo.builder()
//                .method("onMessage")
//                .args(new Object[]{message})
//                .build();
//
//        RabbitMqMessageListenerTracingInterceptor interceptor = new RabbitMqMessageListenerTracingInterceptor(tracing, config);
//
//        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
//        interceptor.before(methodInfo, context, chain);
//        interceptor.after(methodInfo, context, chain);
//
//        Map<String, String> expectedMap = new HashMap<>();
//        expectedMap.put("rabbit.exchange", "mock-exchange");
//        expectedMap.put("rabbit.routing_key", "mock-routingKey");
//        expectedMap.put("rabbit.broker", "mock-uri");
//        expectedMap.put("rabbit.queue", "mock-queue");
//
//        Assert.assertEquals(expectedMap, spanInfoMap);
//    }
//
//    @Test
//    public void disableTracing() {
//        Config config = this.createConfig(RabbitMqMessageListenerTracingInterceptor.ENABLE_KEY, "false");
//        Map<String, String> spanInfoMap = new HashMap<>();
//        Tracing tracing = Tracing.newBuilder()
//                .currentTraceContext(currentTraceContext)
//                .addSpanHandler(new SpanHandler() {
//                    @Override
//                    public boolean end(TraceContext context, MutableSpan span, Cause cause) {
//                        Map<String, String> tmpMap = new HashMap<>(span.tags());
//                        spanInfoMap.putAll(tmpMap);
//                        return super.end(context, span, cause);
//                    }
//                }).build();
//
//
//        Map<Object, Object> context = ContextUtils.createContext();
//        context.put(ContextCons.MQ_URI, "mock-uri");
//
//        MessageProperties messageProperties = new MessageProperties();
//        messageProperties.setConsumerQueue("mock-queue");
//        messageProperties.setReceivedExchange("mock-exchange");
//        messageProperties.setReceivedRoutingKey("mock-routingKey");
//
//        Message message = new Message(new byte[0], messageProperties);
//        MethodInfo methodInfo = MethodInfo.builder()
//                .method("onMessage")
//                .args(new Object[]{message})
//                .build();
//
//        RabbitMqMessageListenerTracingInterceptor interceptor = new RabbitMqMessageListenerTracingInterceptor(tracing, config);
//
//        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
//        interceptor.before(methodInfo, context, chain);
//        interceptor.after(methodInfo, context, chain);
//
//        Assert.assertTrue(spanInfoMap.isEmpty());
//    }
//}
