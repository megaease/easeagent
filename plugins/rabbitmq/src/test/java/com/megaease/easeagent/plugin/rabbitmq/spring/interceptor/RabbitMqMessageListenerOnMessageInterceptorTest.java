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

package com.megaease.easeagent.plugin.rabbitmq.spring.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqMessageListenerOnMessageInterceptorTest {

    @Test
    public void before() {
        RabbitMqMessageListenerOnMessageInterceptor interceptor = new RabbitMqMessageListenerOnMessageInterceptor();
        MessageProperties messageProperties = new MessageProperties();
        String testMqUri = "testMqUri";
        messageProperties.setHeader(ContextCons.MQ_URI, testMqUri);
        Message message = new Message("testBody".getBytes(), messageProperties);
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{message}).build();
        Context context = EaseAgent.getContext();
        interceptor.before(methodInfo, context);
        assertEquals(testMqUri, context.get(ContextCons.MQ_URI));
        context.remove(ContextCons.MQ_URI);

        methodInfo = MethodInfo.builder().args(new Object[]{Collections.singletonList(message)}).build();
        interceptor.before(methodInfo, context);
        assertEquals(testMqUri, context.get(ContextCons.MQ_URI));
    }

    @Test
    public void order() {
        RabbitMqMessageListenerOnMessageInterceptor interceptor = new RabbitMqMessageListenerOnMessageInterceptor();
        assertEquals(Order.HIGHEST.getOrder(), interceptor.order());
    }
}
