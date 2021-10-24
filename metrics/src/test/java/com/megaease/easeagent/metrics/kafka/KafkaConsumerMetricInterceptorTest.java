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

package com.megaease.easeagent.metrics.kafka;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.BaseMetricsTest;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.TopicPartition;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;

public class KafkaConsumerMetricInterceptorTest extends BaseMetricsTest {
    MetricRegistry metricRegistry;
    KafkaMetric kafkaMetric;
    KafkaConsumerMetricInterceptor interceptor;
    String topic = "topic";
    MethodInfo methodInfo;

    @Before
    public void before() {
        Config config = this.createConfig(KafkaConsumerMetricInterceptor.ENABLE_KEY, "true");
        metricRegistry = new MetricRegistry();
        kafkaMetric = new KafkaMetric(metricRegistry);
        interceptor = new KafkaConsumerMetricInterceptor(kafkaMetric, config);
        Map<TopicPartition, List<ConsumerRecord<String, String>>> recordsMap = new HashMap<>();
        List<ConsumerRecord<String, String>> consumerRecordList = new ArrayList<>();
        consumerRecordList.add(new ConsumerRecord<>(topic, 1, 0, "key", "value"));
        recordsMap.put(new TopicPartition(topic, 1), consumerRecordList);
        ConsumerRecords<String, String> consumerRecords = new ConsumerRecords<>(recordsMap);
        methodInfo = MethodInfo.builder()
                .invoker(this)
                .retValue(consumerRecords)
                .build();
    }

    @Test
    public void invokeSuccess() {
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .counterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.CONSUMER)).getCount());
    }

    @Test
    public void invokeFail() {
        Map<Object, Object> context = ContextUtils.createContext();
        methodInfo.setThrowable(new Exception());
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(KafkaConsumerMetricInterceptor.ENABLE_KEY, "false");
        interceptor = new KafkaConsumerMetricInterceptor(kafkaMetric, config);
        Map<Object, Object> context = ContextUtils.createContext();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }


}
