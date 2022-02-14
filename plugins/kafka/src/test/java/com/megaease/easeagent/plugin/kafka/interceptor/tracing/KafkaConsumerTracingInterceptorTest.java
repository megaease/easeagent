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
import com.megaease.easeagent.mock.report.MockSpan;
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaTestUtils;
import com.megaease.easeagent.plugin.kafka.interceptor.MockKafkaConsumer;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import com.megaease.easeagent.plugin.kafka.interceptor.redirect.KafkaAbstractConfigConstructInterceptor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

@MockEaseAgent
public class KafkaConsumerTracingInterceptorTest {
    String topic1 = "testTopic1";
    String topic2 = "testTopic2";


    @Test
    public void doAfter() {
        KafkaConsumerTracingInterceptor interceptor = new KafkaConsumerTracingInterceptor();
        MockKafkaConsumer kafkaConsumer = MockKafkaConsumer.buildOne();
        String topic = "testTopic1";

        MethodInfo methodInfo = MethodInfo.builder().invoker(kafkaConsumer).throwable(new RuntimeException("testError")).build();
        ReportMock.cleanLastSpan();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(ReportMock.getLastSpan());

        methodInfo = MethodInfo.builder().invoker(kafkaConsumer).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(ReportMock.getLastSpan());

        methodInfo = MethodInfo.builder().invoker(kafkaConsumer).retValue(new ConsumerRecords<>(Collections.emptyMap())).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNull(ReportMock.getLastSpan());


        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(
            Collections.singletonMap(new TopicPartition(topic, 1),
                Collections.singletonList(record(topic, 0)))
        );

        methodInfo = MethodInfo.builder().invoker(kafkaConsumer).retValue(consumerRecords).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        MockSpan mockSpan = ReportMock.getLastSpan();
        checkBaseInfo(mockSpan, topic, (String) kafkaConsumer.getEaseAgent$$DynamicField$$Data());

    }

    private ConsumerRecord<String, String> record(String topic, long offset) {
        return new ConsumerRecord<>(topic, 1, offset, "", "");
    }

    private List<ConsumerRecord<String, String>> tenRecords(String topic) {
        return records(topic, 10);
    }


    private List<ConsumerRecord<String, String>> records(String topic, int count) {
        List<ConsumerRecord<String, String>> records = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            records.add(record(topic, i));
        }
        return records;
    }


    @Test
    public void afterPoll() {
        KafkaConsumerTracingInterceptor interceptor = new KafkaConsumerTracingInterceptor();
        MockKafkaConsumer kafkaConsumer = MockKafkaConsumer.buildOne();

        String topic = "testTopic1";
        String uri = (String) kafkaConsumer.getEaseAgent$$DynamicField$$Data();
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(
            Collections.singletonMap(new TopicPartition(topic, 1),
                Collections.singletonList(record(topic, 0)))
        );
        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        checkBaseInfo(Objects.requireNonNull(ReportMock.getLastSpan()), topic, uri);

        List<MockSpan> mockSpans = new ArrayList<>();
        ReportMock.setSpanReportMock(mockSpans::add);
        consumerRecords = new ConsumerRecords<>(
            Collections.singletonMap(new TopicPartition(topic, 1),
                tenRecords(topic)
            ));
        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(1, mockSpans.size());
        checkBaseInfo(mockSpans.get(0), topic, uri);
        mockSpans.clear();

        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(10, mockSpans.size());
        for (MockSpan mockSpan : mockSpans) {
            checkBaseInfo(mockSpan, topic, uri);
        }
        mockSpans.clear();


        ReportMock.setSpanReportMock(mockSpans::add);
        consumerRecords = new ConsumerRecords<>(
            Collections.singletonMap(new TopicPartition(topic, 1),
                tenRecords(topic)
            ));
        interceptor.singleRootSpanOnReceiveBatch = false;
        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(10, mockSpans.size());
        for (MockSpan mockSpan : mockSpans) {
            checkBaseInfo(mockSpan, topic, uri);
        }
        mockSpans.clear();
    }

    private void checkTowTopicMockSpans(List<MockSpan> mockSpans, String uri) {
        Set<String> topics = new HashSet<>();
        for (MockSpan mockSpan : mockSpans) {
            topics.add(mockSpan.tag(KafkaTags.KAFKA_TOPIC_TAG));
        }
        assertTrue(topics.contains(topic1));
        assertTrue(topics.contains(topic2));
        for (MockSpan mockSpan : mockSpans) {
            String topic = mockSpan.tag(KafkaTags.KAFKA_TOPIC_TAG);
            checkBaseInfo(mockSpan, topic, uri);
        }
    }

    public Map<TopicPartition, List<ConsumerRecord<String, String>>> towTopicAndTenRecords() {
        Map<TopicPartition, List<ConsumerRecord<String, String>>> result = new HashMap<>();
        result.put(new TopicPartition(topic1, 1), records(topic1, 5));
        result.put(new TopicPartition(topic2, 1), records(topic2, 5));
        return result;
    }

    @Test
    public void afterPoll2() {
        KafkaConsumerTracingInterceptor interceptor = new KafkaConsumerTracingInterceptor();
        MockKafkaConsumer kafkaConsumer = MockKafkaConsumer.buildOne();

        Map<TopicPartition, List<ConsumerRecord<String, String>>> records = new HashMap<>();
        records.put(new TopicPartition(topic1, 1), Collections.singletonList(record(topic1, 0)));
        records.put(new TopicPartition(topic2, 1), Collections.singletonList(record(topic2, 0)));

        List<MockSpan> mockSpans = new ArrayList<>();
        ReportMock.setSpanReportMock(mockSpans::add);

        String uri = (String) kafkaConsumer.getEaseAgent$$DynamicField$$Data();
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(records);
        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(2, mockSpans.size());
        checkTowTopicMockSpans(mockSpans, uri);
        mockSpans.clear();


        consumerRecords = new ConsumerRecords<>(towTopicAndTenRecords());
        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(2, mockSpans.size());
        checkTowTopicMockSpans(mockSpans, uri);
        mockSpans.clear();

        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(10, mockSpans.size());
        checkTowTopicMockSpans(mockSpans, uri);
        mockSpans.clear();


        ReportMock.setSpanReportMock(mockSpans::add);
        consumerRecords = new ConsumerRecords<>(towTopicAndTenRecords());
        interceptor.singleRootSpanOnReceiveBatch = false;
        interceptor.afterPoll(EaseAgent.getContext(), consumerRecords, uri);
        assertEquals(10, mockSpans.size());
        checkTowTopicMockSpans(mockSpans, uri);
        mockSpans.clear();
    }

    private void checkBaseInfo(MockSpan mockSpan, String topic, String uri) {
        assertEquals(topic, mockSpan.tag(KafkaTags.KAFKA_TOPIC_TAG));
        assertEquals(uri, mockSpan.tag(KafkaTags.KAFKA_BROKER_TAG));
        assertEquals(Type.KAFKA.getRemoteType(), mockSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
        assertEquals(KafkaConsumerTracingInterceptor.REMOTE_SERVICE_NAME, mockSpan.remoteServiceName());
        assertNull(mockSpan.tag("label.remote"));
    }

    @Test
    public void setConsumerSpan() {
        KafkaConsumerTracingInterceptor interceptor = new KafkaConsumerTracingInterceptor();
        Context context = EaseAgent.getContext();
        Span span = context.nextSpan().start();
        String topic = "testTopic";
        String uri = "testUri";
        interceptor.setConsumerSpan(topic, uri, span);
        span.finish();
        MockSpan mockSpan = ReportMock.getLastSpan();
        checkBaseInfo(mockSpan, topic, uri);

        KafkaAbstractConfigConstructInterceptor kafkaAbstractConfigConstructInterceptor = new KafkaAbstractConfigConstructInterceptor();
        KafkaTestUtils.mockRedirect(() -> {
            Properties props = new Properties();
            props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

            MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{props}).build();
            kafkaAbstractConfigConstructInterceptor.doBefore(methodInfo, EaseAgent.getContext());

            MockKafkaConsumer kafkaConsumer = new MockKafkaConsumer(props);
            kafkaConsumer.setEaseAgent$$DynamicField$$Data(props.getProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));

            Span span1 = context.nextSpan().start();
            interceptor.setConsumerSpan(topic, (String) kafkaConsumer.getEaseAgent$$DynamicField$$Data(), span1);
            span1.finish();
            MockSpan mockSpan1 = ReportMock.getLastSpan();
            assertEquals(TestConst.REDIRECT_URIS, mockSpan1.tag("label.remote"));
        });
    }
}
