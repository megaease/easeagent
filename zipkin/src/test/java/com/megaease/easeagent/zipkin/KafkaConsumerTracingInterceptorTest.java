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
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.kafka.v2d3.KafkaConsumerTracingInterceptor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class KafkaConsumerTracingInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Config config = this.createConfig(KafkaConsumerTracingInterceptor.ENABLE_KEY, "true");
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

        Consumer<String, String> consumer = mock(Consumer.class, withSettings().extraInterfaces(DynamicFieldAccessor.class));
        DynamicFieldAccessor accessor = (DynamicFieldAccessor) consumer;
        when(accessor.getEaseAgent$$DynamicField$$Data()).thenReturn("mock-uri");
        String topic = "mock-topic";
        TopicPartition topicPartition = new TopicPartition(topic, 1);
        List<ConsumerRecord<String, String>> list = new ArrayList<>();
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 0, "mock-key", "mock-value");
        list.add(consumerRecord);
        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordMap = new HashMap<>();
        recordMap.put(topicPartition, list);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(recordMap);
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(consumer)
                .method("poll")
                .args(new Object[]{})
                .retValue(consumerRecords)
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);

        KafkaConsumerTracingInterceptor interceptor = new KafkaConsumerTracingInterceptor(tracing, config);
        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Map<String, String> expectedMap = new HashMap<>();
        expectedMap.put("kafka.topic", topic);
        expectedMap.put("kafka.broker", "mock-uri");
        Assert.assertEquals(expectedMap, spanInfoMap);
    }

    @Test
    public void disableTracing() {
        Config config = this.createConfig(KafkaConsumerTracingInterceptor.ENABLE_KEY, "false");
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

        Consumer<String, String> consumer = mock(Consumer.class, withSettings().extraInterfaces(DynamicFieldAccessor.class));
        DynamicFieldAccessor accessor = (DynamicFieldAccessor) consumer;
        when(accessor.getEaseAgent$$DynamicField$$Data()).thenReturn("mock-uri");
        String topic = "mock-topic";
        TopicPartition topicPartition = new TopicPartition(topic, 1);
        List<ConsumerRecord<String, String>> list = new ArrayList<>();
        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 0, "mock-key", "mock-value");
        list.add(consumerRecord);
        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordMap = new HashMap<>();
        recordMap.put(topicPartition, list);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(recordMap);
        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(consumer)
                .method("poll")
                .args(new Object[]{})
                .retValue(consumerRecords)
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);

        KafkaConsumerTracingInterceptor interceptor = new KafkaConsumerTracingInterceptor(tracing, config);
        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Assert.assertTrue(spanInfoMap.isEmpty());
    }
}
