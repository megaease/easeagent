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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor;

import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.rabbitmq.client.AMQP;
import org.junit.Test;

import static org.junit.Assert.*;

public class RabbitMqConsumerHandleDeliveryInterceptorTest {

    @Test
    public void before() {
        RabbitMqConsumerHandleDeliveryInterceptor interceptor = new RabbitMqConsumerHandleDeliveryInterceptor();
        AMQP.BasicProperties properties = new AMQP.BasicProperties();
        MockConsumer mockConsumer = new MockConsumer(null);
        String data = "192.168.0.13:2222";
        mockConsumer.setEaseAgent$$DynamicField$$Data(data);
        MethodInfo methodInfo = MethodInfo.builder().invoker(mockConsumer).args(new Object[]{null, null, properties}).build();
        interceptor.before(methodInfo, EaseAgent.getContext());
        assertEquals(data, properties.getHeaders().get(ContextCons.MQ_URI));

    }

    @Test
    public void order() {
        RabbitMqConsumerHandleDeliveryInterceptor interceptor = new RabbitMqConsumerHandleDeliveryInterceptor();
        assertEquals(Order.HIGHEST.getOrder(), interceptor.order());
    }
}
