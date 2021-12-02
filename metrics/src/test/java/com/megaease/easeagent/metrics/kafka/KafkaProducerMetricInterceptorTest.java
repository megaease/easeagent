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
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.BaseMetricsTest;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class KafkaProducerMetricInterceptorTest extends BaseMetricsTest {

    MetricRegistry metricRegistry;
    KafkaMetric kafkaMetric;
    KafkaProducerMetricInterceptor interceptor;
    String topic = "topic";
    ProducerRecord<String, String> producerRecord;
    MethodInfo methodInfo;

    @Before
    public void before() {
        Config config = this.createConfig(KafkaProducerMetricInterceptor.ENABLE_KEY, "true");
        metricRegistry = new MetricRegistry();
        kafkaMetric = new KafkaMetric(metricRegistry);
        interceptor = new KafkaProducerMetricInterceptor(kafkaMetric, config);
        producerRecord = new ProducerRecord<>(topic, 1, System.currentTimeMillis(), "key", "value");
        methodInfo = MethodInfo.builder()
                .invoker(this)
                .args(new Object[]{producerRecord})
                .build();
    }

    @Test
    public void invokeSuccessAsync() {
        NameFactory metricNameFactory = NameFactory.createBuilder()
                .timerType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.ASYNC_FLAG, true);

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER)).getCount());
    }

    @Test
    public void invokeSuccessSyncErr() {
        NameFactory metricNameFactory = NameFactory.createBuilder()
                .meterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        methodInfo.setThrowable(new Exception());

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
    }

    @Test
    public void invokeSuccessAsyncErr() {
        NameFactory metricNameFactory = NameFactory.createBuilder()
                .timerType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .counterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .build();
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.ASYNC_FLAG, true);
        methodInfo.setThrowable(new Exception());

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(topic, MetricSubType.PRODUCER_ERROR)).getCount());
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(KafkaProducerMetricInterceptor.ENABLE_KEY, "false");
        interceptor = new KafkaProducerMetricInterceptor(kafkaMetric, config);
        Map<Object, Object> context = ContextUtils.createContext();
        context.put(ContextCons.ASYNC_FLAG, true);

        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }
}
