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
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class KafkaMessageListenerMetricInterceptorTest extends BaseMetricsTest {
    MetricRegistry metricRegistry;
    KafkaMetric kafkaMetric;
    KafkaMessageListenerMetricInterceptor interceptor;
    String topic = "topic";
    MethodInfo methodInfo;

    @Before
    public void before() {
        Config config = this.createConfig(KafkaMessageListenerMetricInterceptor.ENABLE_KEY, "true");
        metricRegistry = new MetricRegistry();
        kafkaMetric = new KafkaMetric(metricRegistry);
        interceptor = new KafkaMessageListenerMetricInterceptor(kafkaMetric, config);

        ConsumerRecord<String, String> consumerRecord = new ConsumerRecord<>(topic, 1, 0, "key", "value");

        methodInfo = MethodInfo.builder()
                .args(new Object[]{consumerRecord})
                .build();
    }

    @Test
    public void invokeSuccess() {
        NameFactory metricNameFactory = NameFactory.createBuilder()
                .timerType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .counterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();

        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);

        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.CONSUMER)).getCount());
    }

    @Test
    public void invokeFail() {
        Map<Object, Object> context = ContextUtils.createContext();
        methodInfo.setThrowable(new Exception());
        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);

        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        NameFactory metricNameFactory = NameFactory.createBuilder()
                .timerType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER_ERROR, Maps.newHashMap())
                .counterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .counterType(MetricSubType.CONSUMER_ERROR, Maps.newHashMap())
                .build();

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER_ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.CONSUMER_ERROR)).getCount());

    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(KafkaMessageListenerMetricInterceptor.ENABLE_KEY, "false");
        metricRegistry = new MetricRegistry();
        kafkaMetric = new KafkaMetric(metricRegistry);
        interceptor = new KafkaMessageListenerMetricInterceptor(kafkaMetric, config);
        Map<Object, Object> context = ContextUtils.createContext();
        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);

        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }
}
