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

package com.megaease.easeagent.plugin.kafka.interceptor.initialize;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.interceptor.MockConsumerRecord;
import com.megaease.easeagent.plugin.kafka.interceptor.MockKafkaConsumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class KafkaConsumerPollInterceptorTest {

    @Test
    public void doAfter() {
        KafkaConsumerPollInterceptor interceptor = new KafkaConsumerPollInterceptor();
        MockKafkaConsumer kafkaConsumer = MockKafkaConsumer.buildOne();
        String topic = "testTopic";
        MockConsumerRecord mockConsumerRecord = MockConsumerRecord.buldOne(topic, 0);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(
            Collections.singletonMap(new TopicPartition(topic, 1),
                Collections.singletonList(mockConsumerRecord))
        );
        assertNull(mockConsumerRecord.getEaseAgent$$DynamicField$$Data());

        MethodInfo methodInfo = MethodInfo.builder().invoker(kafkaConsumer).retValue(consumerRecords).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        assertNotNull(mockConsumerRecord.getEaseAgent$$DynamicField$$Data());
        assertEquals(kafkaConsumer.getEaseAgent$$DynamicField$$Data(), mockConsumerRecord.getEaseAgent$$DynamicField$$Data());

    }

    @Test
    public void order() {
        KafkaConsumerPollInterceptor interceptor = new KafkaConsumerPollInterceptor();
        assertEquals(Order.TRACING_INIT.getOrder(), interceptor.order());

    }

    @Test
    public void getType() {
        KafkaConsumerPollInterceptor interceptor = new KafkaConsumerPollInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }
}
