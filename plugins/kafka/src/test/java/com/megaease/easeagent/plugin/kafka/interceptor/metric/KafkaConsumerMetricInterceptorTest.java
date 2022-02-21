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

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.megaease.easeagent.mock.metrics.MockMetricUtils;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaConsumerMetricInterceptorTest {

    @Test
    public void init() {
        KafkaConsumerMetricInterceptor interceptor = new KafkaConsumerMetricInterceptor();
        KafkaMetricTest.init(interceptor);
        assertNotNull(KafkaConsumerMetricInterceptor.getKafkaMetric());

    }

    public static ConsumerRecord<String, String> record(String topic, long offset) {
        return new ConsumerRecord<>(topic, 1, offset, "", "");
    }

    @Test
    public void doAfter() {
        MockMetricUtils.clearAll();

        KafkaConsumerMetricInterceptor interceptor = new KafkaConsumerMetricInterceptor();
        KafkaMetricTest.init(interceptor);

        String topic = "testTopic1";
        MethodInfo methodInfo = MethodInfo.builder().throwable(new RuntimeException("testError")).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());

        methodInfo = MethodInfo.builder().build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());

        methodInfo = MethodInfo.builder().retValue(new ConsumerRecords<>(Collections.emptyMap())).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());

        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(
            Collections.singletonMap(new TopicPartition(topic, 1),
                Collections.singletonList(record(topic, 0)))
        );

        methodInfo = MethodInfo.builder().retValue(consumerRecords).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());


        LastJsonReporter lastJsonReporter = KafkaMetricTest.lastMetricSupplier(topic);
        lastJsonReporter.clean();
        Map<String, Object> metric = KafkaMetricTest.waitOne(lastJsonReporter);
        lastJsonReporter.clean();
        metric = KafkaMetricTest.waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_COUNT.getField()));

    }

    @Test
    public void getType() {
        KafkaConsumerMetricInterceptor interceptor = new KafkaConsumerMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }
}
