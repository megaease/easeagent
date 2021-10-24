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

package com.megaease.easeagent.metrics.rabbitmq;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Maps;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.BaseMetricsTest;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import com.rabbitmq.client.Envelope;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class RabbitMqConsumerInterceptorTest extends BaseMetricsTest {

    @Test
    public void invokeSuccess() {
        Config config = this.createConfig(RabbitMqConsumerMetricInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .build();
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor(metric, config);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"", envelope}).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = envelope.getRoutingKey();
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER)).getCount());
    }

    @Test
    public void invokeErr() {
        Config config = this.createConfig(RabbitMqConsumerMetricInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER_ERROR, Maps.newHashMap())
                .build();
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor(metric, config);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"", envelope}).throwable(new Exception()).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = envelope.getRoutingKey();
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER_ERROR)).getCount());
    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(RabbitMqConsumerMetricInterceptor.ENABLE_KEY, "false");
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric metric = new RabbitMqConsumerMetric(metricRegistry);
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor(metric, config);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        Envelope envelope = new Envelope(1, true, "exchange", "routingKey");
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"", envelope}).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }
}
