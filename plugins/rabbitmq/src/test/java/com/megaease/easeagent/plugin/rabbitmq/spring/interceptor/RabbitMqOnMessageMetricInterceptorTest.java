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

package com.megaease.easeagent.plugin.rabbitmq.spring.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqConsumerMetric;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Arrays;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqOnMessageMetricInterceptorTest {
    private static final Object START_KEY = AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqOnMessageMetricInterceptor.class, "START");

    @Test
    public void init() {
        RabbitMqOnMessageMetricInterceptor interceptor = new RabbitMqOnMessageMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new RabbitMqPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqOnMessageMetricInterceptor.class, "metric"));
    }

    @Test
    public void before() {
        RabbitMqOnMessageMetricInterceptor interceptor = new RabbitMqOnMessageMetricInterceptor();
        interceptor.before(null, EaseAgent.getContext());
        assertNotNull(EaseAgent.getContext().get(START_KEY));
    }

    @Test
    public void after() {
        RabbitMqOnMessageMetricInterceptor interceptor = new RabbitMqOnMessageMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new RabbitMqPlugin());

        MessageProperties messageProperties = new MessageProperties();
        String queue = "testConsumerQueue";
        messageProperties.setConsumerQueue(queue);
        String testMqUri = "testMqUri";
        messageProperties.setHeader(ContextCons.MQ_URI, testMqUri);
        Message message = new Message("testBody".getBytes(), messageProperties);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        Context context = EaseAgent.getContext();
        context.put(START_KEY, System.currentTimeMillis() - 100);
        interceptor.after(methodInfo, context);

        RabbitMqConsumerMetric metric = AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqOnMessageMetricInterceptor.class, "metric");
        Meter meter = metric.meter(queue, MetricSubType.CONSUMER);
        Meter meterError = metric.meter(queue, MetricSubType.CONSUMER_ERROR);
        assertEquals(1, meter.getCount());
        assertEquals(0, meterError.getCount());

        methodInfo = MethodInfo.builder().args(new Object[]{Arrays.asList(message, message)}).build();
        interceptor.after(methodInfo, context);
        assertEquals(3, meter.getCount());
        assertEquals(0, meterError.getCount());

        methodInfo = MethodInfo.builder().args(new Object[]{message}).throwable(new RuntimeException("testError")).build();
        interceptor.after(methodInfo, context);
        assertEquals(4, meter.getCount());
        assertEquals(1, meterError.getCount());

        methodInfo = MethodInfo.builder().args(new Object[]{Arrays.asList(message, message)}).throwable(new RuntimeException("testError")).build();
        interceptor.after(methodInfo, context);
        assertEquals(6, meter.getCount());
        assertEquals(3, meterError.getCount());
    }

    @Test
    public void getType() {
        RabbitMqOnMessageMetricInterceptor interceptor = new RabbitMqOnMessageMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }

    @Test
    public void order() {
        RabbitMqOnMessageMetricInterceptor interceptor = new RabbitMqOnMessageMetricInterceptor();
        assertEquals(Order.METRIC.getOrder(), interceptor.order());
    }
}
