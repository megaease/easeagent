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
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Map;

import static org.mockito.Mockito.mock;

public class RabbitMqMessageListenerMetricInterceptorTest extends BaseMetricsTest {

    @Test
    public void success() {
        Config config = this.createConfig(RabbitMqMessageListenerMetricInterceptor.ENABLE_KEY, "true");
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric rabbitMqConsumerMetric = new RabbitMqConsumerMetric(metricRegistry);
        RabbitMqMessageListenerMetricInterceptor interceptor = new RabbitMqMessageListenerMetricInterceptor(rabbitMqConsumerMetric, config);

        NameFactory metricNameFactory = NameFactory.createBuilder()
                .timerType(MetricSubType.DEFAULT, Maps.newHashMap())
                .meterType(MetricSubType.CONSUMER, Maps.newHashMap())
                .build();

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setConsumerQueue("mock-queue");
        Message message = new Message(new byte[0], messageProperties);
        MethodInfo methodInfo = MethodInfo.builder()
                .args(new Object[]{message})
                .build();

        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);

        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        String key = messageProperties.getConsumerQueue();
        Assert.assertEquals(1L, metricRegistry.timer(metricNameFactory.timerName(key, MetricSubType.DEFAULT)).getCount());
        Assert.assertEquals(1L, metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.CONSUMER)).getCount());

    }

    @Test
    public void disableCollect() {
        Config config = this.createConfig(RabbitMqMessageListenerMetricInterceptor.ENABLE_KEY, "false");
        MetricRegistry metricRegistry = new MetricRegistry();
        RabbitMqConsumerMetric rabbitMqConsumerMetric = new RabbitMqConsumerMetric(metricRegistry);
        RabbitMqMessageListenerMetricInterceptor interceptor = new RabbitMqMessageListenerMetricInterceptor(rabbitMqConsumerMetric, config);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setConsumerQueue("mock-queue");
        Message message = new Message(new byte[0], messageProperties);
        MethodInfo methodInfo = MethodInfo.builder()
                .args(new Object[]{message})
                .build();

        Map<Object, Object> context = ContextUtils.createContext();
        ContextUtils.setEndTime(context);

        AgentInterceptorChain chain = mock(AgentInterceptorChain.class);
        interceptor.before(methodInfo, context, chain);
        interceptor.after(methodInfo, context, chain);

        Assert.assertTrue(metricRegistry.getMetrics().isEmpty());
    }

}
