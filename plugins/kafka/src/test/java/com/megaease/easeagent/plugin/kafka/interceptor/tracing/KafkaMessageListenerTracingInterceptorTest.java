/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.kafka.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaTestUtils;
import com.megaease.easeagent.plugin.kafka.interceptor.MockConsumerRecord;
import com.megaease.easeagent.plugin.kafka.interceptor.MockKafkaConsumer;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import com.megaease.easeagent.plugin.kafka.interceptor.redirect.KafkaAbstractConfigConstructInterceptor;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaMessageListenerTracingInterceptorTest {
    String topic = "testTopic";

    private ConsumerRecord<String, String> createConsumerRecord(MockKafkaConsumer kafkaConsumer) {
        MockConsumerRecord mockConsumerRecord = MockConsumerRecord.buldOne(topic, 0);
        mockConsumerRecord.setEaseAgent$$DynamicField$$Data(kafkaConsumer.getEaseAgent$$DynamicField$$Data());
        return mockConsumerRecord;
    }

    private void check(ReportSpan mockSpan, String broker) {
        assertEquals("on-message", mockSpan.name());
        assertEquals(Span.Kind.CLIENT.name(), mockSpan.kind());
        assertEquals("kafka", mockSpan.remoteServiceName());
        assertEquals(broker, mockSpan.tag(KafkaTags.KAFKA_BROKER_TAG));
        assertEquals(Type.KAFKA.getRemoteType(), mockSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
    }

    @Test
    public void doBefore() {
        KafkaMessageListenerTracingInterceptor interceptor = new KafkaMessageListenerTracingInterceptor();
        Context context = EaseAgent.getOrCreateTracingContext();
        MockKafkaConsumer kafkaConsumer = MockKafkaConsumer.buildOne();

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{createConsumerRecord(kafkaConsumer)}).build();
        interceptor.doBefore(methodInfo, context);
        context.<Span>remove(KafkaMessageListenerTracingInterceptor.SPAN).finish();
        check(MockEaseAgent.getLastSpan(), (String) kafkaConsumer.getEaseAgent$$DynamicField$$Data());
    }

    @Test
    public void doBeforeRedirected() {
        KafkaAbstractConfigConstructInterceptor kafkaAbstractConfigConstructInterceptor = new KafkaAbstractConfigConstructInterceptor();
        Context context = EaseAgent.getOrCreateTracingContext();
        KafkaTestUtils.mockRedirect(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            MethodInfo constructMethodInfo = MethodInfo.builder().args(new Object[]{props}).build();
            kafkaAbstractConfigConstructInterceptor.doBefore(constructMethodInfo, EaseAgent.getOrCreateTracingContext());

            MockKafkaConsumer kafkaConsumer = new MockKafkaConsumer(props);
            kafkaConsumer.setEaseAgent$$DynamicField$$Data(props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));


            KafkaMessageListenerTracingInterceptor interceptor = new KafkaMessageListenerTracingInterceptor();
            MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{createConsumerRecord(kafkaConsumer)}).build();
            interceptor.doBefore(methodInfo, context);
            context.<Span>remove(KafkaMessageListenerTracingInterceptor.SPAN).finish();
            check(MockEaseAgent.getLastSpan(), (String) kafkaConsumer.getEaseAgent$$DynamicField$$Data());
            assertEquals(TestConst.REDIRECT_URIS, MockEaseAgent.getLastSpan().tag("label.remote"));
        });

    }

    @Test
    public void doAfter() {
        KafkaMessageListenerTracingInterceptor interceptor = new KafkaMessageListenerTracingInterceptor();
        Context context = EaseAgent.getOrCreateTracingContext();
        MockEaseAgent.cleanLastSpan();
        interceptor.doAfter(null, context);
        assertNull(MockEaseAgent.getLastSpan());
        Span span = context.nextSpan().start();
        context.put(KafkaMessageListenerTracingInterceptor.SPAN, span);
        MethodInfo methodInfo = MethodInfo.builder().build();
        interceptor.doAfter(methodInfo, context);
        ReportSpan mockSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);

        String errorInfo = "test error";
        methodInfo = MethodInfo.builder().throwable(new RuntimeException(errorInfo)).build();
        span = context.nextSpan().start();
        context.put(KafkaMessageListenerTracingInterceptor.SPAN, span);
        interceptor.doAfter(methodInfo, context);
        mockSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);
        assertTrue(mockSpan.tags().containsKey("error"));
        assertEquals(errorInfo, mockSpan.tags().get("error"));

    }
}
