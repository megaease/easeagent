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

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaTestUtils;
import com.megaease.easeagent.plugin.kafka.interceptor.MockKafkaProducer;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import com.megaease.easeagent.plugin.kafka.interceptor.redirect.KafkaAbstractConfigConstructInterceptor;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Properties;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaProducerDoSendInterceptorTest {
    String topic = "testTopic";
    String key = "testKey";
    String value = "testValue";


    Span finishSpan() {
        Context context = EaseAgent.getContext();
        Span span = context.remove(KafkaProducerDoSendInterceptor.SPAN);
        span.finish();
        context.<Scope>remove(KafkaProducerDoSendInterceptor.SCOPE).close();
        return span;
    }

    private void checkBaseInfo(ReportSpan mockSpan) {
        assertEquals(TestConst.URIS, mockSpan.tag(KafkaTags.KAFKA_BROKER_TAG));
        assertEquals(Span.Kind.PRODUCER.name(), mockSpan.kind());
        assertEquals("send", mockSpan.name());
        assertEquals(KafkaProducerDoSendInterceptor.REMOTE_SERVICE_NAME, mockSpan.remoteServiceName());
        assertEquals(topic, mockSpan.tag(KafkaTags.KAFKA_TOPIC_TAG));
        assertEquals(Type.KAFKA.getRemoteType(), mockSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
        assertNull(mockSpan.tag("label.remote"));
    }

    @Test
    public void doBefore() {
        KafkaProducerDoSendInterceptor interceptor = new KafkaProducerDoSendInterceptor();
        MockKafkaProducer kafkaProducer = MockKafkaProducer.buildOne();
        Context context = EaseAgent.getContext();

        ProducerRecord record = new ProducerRecord<>(topic, key, value);
        MethodInfo methodInfo = MethodInfo.builder().invoker(kafkaProducer).args(new Object[]{record, null}).build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(context.currentTracing().hasCurrentSpan());
        Span span = finishSpan();
        assertFalse(context.currentTracing().hasCurrentSpan());
        ReportSpan mockSpan = ReportMock.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);
        checkBaseInfo(mockSpan);
        assertEquals(key, mockSpan.tag(KafkaTags.KAFKA_KEY_TAG));


        record = new ProducerRecord<>(topic, value);
        methodInfo = MethodInfo.builder().invoker(kafkaProducer).args(new Object[]{record, null}).build();
        context = EaseAgent.getContext();
        interceptor.doBefore(methodInfo, context);

        assertTrue(context.currentTracing().hasCurrentSpan());
        span = finishSpan();
        assertFalse(context.currentTracing().hasCurrentSpan());
        mockSpan = ReportMock.getLastSpan();
        SpanTestUtils.sameId(span, mockSpan);
        checkBaseInfo(mockSpan);
        assertNull(mockSpan.tag(KafkaTags.KAFKA_KEY_TAG));


        record = new ProducerRecord<>(topic, value);
        methodInfo = MethodInfo.builder().invoker(kafkaProducer).args(new Object[]{record, null}).build();
        context = EaseAgent.getContext();
        interceptor.doBefore(methodInfo, context);
        assertTrue(context.currentTracing().hasCurrentSpan());
        finishSpan();
        assertFalse(context.currentTracing().hasCurrentSpan());
        assertNotNull(methodInfo.getArgs()[1]);
        assertTrue(methodInfo.getArgs()[1] instanceof TraceCallback);
    }

    @Test
    public void testRedirectedTag() {
        KafkaAbstractConfigConstructInterceptor kafkaAbstractConfigConstructInterceptor = new KafkaAbstractConfigConstructInterceptor();
        KafkaTestUtils.mockRedirect(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
            props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{props}).build();
            kafkaAbstractConfigConstructInterceptor.doBefore(methodInfo, EaseAgent.getContext());

            MockKafkaProducer kafkaProducer = new MockKafkaProducer(props);
            kafkaProducer.setEaseAgent$$DynamicField$$Data(props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

            KafkaProducerDoSendInterceptor interceptor = new KafkaProducerDoSendInterceptor();
            Context context = EaseAgent.getContext();

            ProducerRecord record = new ProducerRecord<>(topic, key, value);
            methodInfo = MethodInfo.builder().invoker(kafkaProducer).args(new Object[]{record, null}).build();
            interceptor.doBefore(methodInfo, context);
            assertTrue(context.currentTracing().hasCurrentSpan());
            finishSpan();
            assertFalse(context.currentTracing().hasCurrentSpan());
            ReportSpan mockSpan = ReportMock.getLastSpan();
            assertEquals(TestConst.REDIRECT_URIS, mockSpan.tag("label.remote"));
        });
    }

    @Test
    public void doAfter() {
        KafkaProducerDoSendInterceptor interceptor = new KafkaProducerDoSendInterceptor();
        Context context = EaseAgent.getContext();
        MockKafkaProducer kafkaProducer = MockKafkaProducer.buildOne();

        interceptor.doAfter(null, context);


        ProducerRecord record = new ProducerRecord<>(topic, key, value);
        MethodInfo methodInfo = MethodInfo.builder().invoker(kafkaProducer).args(new Object[]{record, null}).build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(context.currentTracing().hasCurrentSpan());
        interceptor.doAfter(methodInfo, context);
        assertFalse(context.currentTracing().hasCurrentSpan());
        Callback callback = (Callback) methodInfo.getArgs()[1];
        callback.onCompletion(null, null);
        ReportSpan mockSpan = ReportMock.getLastSpan();
        assertFalse(mockSpan.tags().containsKey("error"));

        record = new ProducerRecord<>(topic, key, value);
        String errorInfo = "test error";
        methodInfo = MethodInfo.builder().invoker(kafkaProducer).args(new Object[]{record, null}).throwable(new RuntimeException(errorInfo)).build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(context.currentTracing().hasCurrentSpan());
        interceptor.doAfter(methodInfo, context);
        assertFalse(context.currentTracing().hasCurrentSpan());
        callback = (Callback) methodInfo.getArgs()[1];
        callback.onCompletion(null, null);

        mockSpan = ReportMock.getLastSpan();
        assertTrue(mockSpan.tags().containsKey("error"));
        assertEquals(errorInfo, mockSpan.tags().get("error"));

    }
}
