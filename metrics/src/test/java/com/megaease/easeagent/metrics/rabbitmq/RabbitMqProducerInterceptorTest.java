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
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class RabbitMqProducerInterceptorTest {

    @Test
    public void invokeSuccess() {
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqProducerMetric metric = new RabbitMqProducerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .build();
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor(metric);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"exchange", "routingKey"}).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = String.join("-", "exchange", "routingKey");
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER)).getCount());
    }

    @Test
    public void invokeErr() {
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqProducerMetric metric = new RabbitMqProducerMetric(metricRegistry);
        MetricNameFactory metricNameFactory = MetricNameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER, Maps.newHashMap())
                .meterType(MetricSubType.PRODUCER_ERROR, Maps.newHashMap())
                .build();
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor(metric);
        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("publish").args(new Object[]{"exchange", "routingKey"}).throwable(new Exception()).build();
        interceptor.after(methodInfo, context, mock(AgentInterceptorChain.class));
        String key = String.join("-", "exchange", "routingKey");
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER_ERROR)).getCount());
    }
}
