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
//import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
//import com.megaease.easeagent.plugin.MethodInfo;
//import com.megaease.easeagent.core.utils.ContextUtils;
//import org.apache.kafka.clients.producer.Callback;
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.junit.Assert;
//import org.junit.Test;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.mockito.Mockito.*;
//
//@SuppressWarnings("unchecked")
//public class KafkaProducerTracingInterceptorTest extends BaseZipkinTest {
//
//    @Test
//    public void success() {
//        Config config = this.createConfig(KafkaProducerTracingInterceptor.ENABLE_KEY, "true");
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
//        context.put(ContextCons.ASYNC_FLAG, true);
//
//        KafkaProducer<String, String> kafkaProducer = mock(KafkaProducer.class, withSettings().extraInterfaces(DynamicFieldAccessor.class));
//        DynamicFieldAccessor accessor = (DynamicFieldAccessor) kafkaProducer;
//        when(accessor.getEaseAgent$$DynamicField$$Data()).thenReturn("mock-uri");
//        String topic = "mock-topic";
//        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "mock-key", "mock-value");
//        MethodInfo methodInfo = MethodInfo.builder()
//                .invoker(kafkaProducer)
//                .method("doSend")
//                .args(new Object[]{producerRecord, null})
//                .build();
//        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
//
//        KafkaProducerTracingInterceptor interceptor = new KafkaProducerTracingInterceptor(tracing, config);
//        interceptor.before(methodInfo, context, chain);
//        interceptor.after(methodInfo, context, chain);
//        Callback callback = (Callback) methodInfo.getArgs()[1];
//        callback.onCompletion(null, null);
//
//        Map<String, String> expectedMap = new HashMap<>();
//        expectedMap.put("kafka.topic", "mock-topic");
//        expectedMap.put("kafka.key", "mock-key");
//        expectedMap.put("kafka.broker", "mock-uri");
//
//        Assert.assertEquals(expectedMap, spanInfoMap);
//    }
//
//    @Test
//    public void disableTracing() {
//        Config config = this.createConfig(KafkaProducerTracingInterceptor.ENABLE_KEY, "false");
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
//        context.put(ContextCons.ASYNC_FLAG, true);
//
//        KafkaProducer<String, String> kafkaProducer = mock(KafkaProducer.class, withSettings().extraInterfaces(DynamicFieldAccessor.class));
//        DynamicFieldAccessor accessor = (DynamicFieldAccessor) kafkaProducer;
//        when(accessor.getEaseAgent$$DynamicField$$Data()).thenReturn("mock-uri");
//        String topic = "mock-topic";
//        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic, "mock-key", "mock-value");
//        MethodInfo methodInfo = MethodInfo.builder()
//                .invoker(kafkaProducer)
//                .method("doSend")
//                .args(new Object[]{producerRecord, null})
//                .build();
//        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
//
//        KafkaProducerTracingInterceptor interceptor = new KafkaProducerTracingInterceptor(tracing, config);
//        interceptor.before(methodInfo, context, chain);
//        interceptor.after(methodInfo, context, chain);
//        Callback callback = (Callback) methodInfo.getArgs()[1];
//        Assert.assertNull(callback);
//        Assert.assertTrue(spanInfoMap.isEmpty());
//    }
//}
