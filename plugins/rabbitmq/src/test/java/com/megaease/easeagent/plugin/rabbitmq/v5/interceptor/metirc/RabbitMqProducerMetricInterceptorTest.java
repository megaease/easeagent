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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.metirc;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqProducerMetric;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqProducerMetricInterceptorTest {

    @Test
    public void init() {
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new RabbitMqPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqProducerMetricInterceptor.class, "metric"));
    }

    @Test
    public void before() {
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor();
        interceptor.before(null, null);
        assertTrue(true);
    }

    @Test
    public void after() {
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new RabbitMqPlugin());
        Context context = EaseAgent.getOrCreateTracingContext();
        ContextUtils.setBeginTime(context);

        String exchange = "testExchange";
        String routingKey = "testRoutingKey";
        String key = String.join("-", exchange, routingKey);

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{exchange, routingKey}).build();
        interceptor.after(methodInfo, context);

        RabbitMqProducerMetric metric = AgentFieldReflectAccessor.getStaticFieldValue(RabbitMqProducerMetricInterceptor.class, "metric");
        Meter meter = metric.meter(key, MetricSubType.PRODUCER);
        Meter meterError = metric.meter(key, MetricSubType.PRODUCER_ERROR);
        assertEquals(1, meter.getCount());
        assertEquals(0, meterError.getCount());


        methodInfo = MethodInfo.builder().args(new Object[]{exchange, routingKey}).throwable(new RuntimeException("testError")).build();
        interceptor.after(methodInfo, context);
        assertEquals(2, meter.getCount());
        assertEquals(1, meterError.getCount());


    }

    @Test
    public void getType() {
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }

    @Test
    public void order() {
        RabbitMqProducerMetricInterceptor interceptor = new RabbitMqProducerMetricInterceptor();
        assertEquals(Order.METRIC.getOrder(), interceptor.order());
    }
}
