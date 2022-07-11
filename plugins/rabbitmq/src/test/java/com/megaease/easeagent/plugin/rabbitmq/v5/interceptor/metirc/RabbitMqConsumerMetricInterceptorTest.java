/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.metirc;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqConsumerMetric;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.rabbitmq.client.Envelope;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqConsumerMetricInterceptorTest {
    private static final Object START_KEY = AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqConsumerMetricInterceptor.class, "START");

    @Test
    public void init() {
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new RabbitMqPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqConsumerMetricInterceptor.class, "metric"));

    }

    @Test
    public void before() {
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor();
        interceptor.before(null, EaseAgent.getContext());
        assertNotNull(EaseAgent.getContext().get(START_KEY));

    }

    @Test
    public void after() {
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new RabbitMqPlugin());

        String queue = "testRabbitMqConsumerMetricInterceptorConsumerQueue";
        Envelope envelope = new Envelope(0, false, "", queue);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{null, envelope}).build();
        Context context = EaseAgent.getContext();
        context.put(START_KEY, System.currentTimeMillis() - 100);
        interceptor.after(methodInfo, context);

        RabbitMqConsumerMetric metric = AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqConsumerMetricInterceptor.class, "metric");
        Meter meter = metric.meter(queue, MetricSubType.CONSUMER);
        Meter meterError = metric.meter(queue, MetricSubType.CONSUMER_ERROR);
        assertEquals(1, meter.getCount());
        assertEquals(0, meterError.getCount());


        methodInfo = MethodInfo.builder().args(new Object[]{null, envelope}).throwable(new RuntimeException("testError")).build();
        interceptor.after(methodInfo, context);
        assertEquals(2, meter.getCount());
        assertEquals(1, meterError.getCount());
    }

    @Test
    public void getType() {
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }

    @Test
    public void order() {
        RabbitMqConsumerMetricInterceptor interceptor = new RabbitMqConsumerMetricInterceptor();
        assertEquals(Order.METRIC.getOrder(), interceptor.order());
    }
}
