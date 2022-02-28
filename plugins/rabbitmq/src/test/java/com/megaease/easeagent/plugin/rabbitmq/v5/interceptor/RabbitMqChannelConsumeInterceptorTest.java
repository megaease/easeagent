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

import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RabbitMqChannelConsumeInterceptorTest {

    @Test
    public void before() throws UnknownHostException {
        RabbitMqChannelConsumeInterceptor interceptor = new RabbitMqChannelConsumeInterceptor();
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.getConnection()).thenReturn(connection);
        String host = "127.0.0.1";
        int port = 1111;
        InetAddress inetAddress = InetAddress.getLocalHost();
        when(connection.getAddress()).thenReturn(inetAddress);
        when(connection.getPort()).thenReturn(port);
        MockConsumer mockConsumer = new MockConsumer(channel);
        interceptor.before(MethodInfo.builder().invoker(channel).args(new Object[]{null, null, null, null, null, null, mockConsumer}).build(), null);
        assertEquals(host + ":" + port, mockConsumer.getEaseAgent$$DynamicField$$Data());
    }


    @Test
    public void order() {
        RabbitMqChannelConsumeInterceptor interceptor = new RabbitMqChannelConsumeInterceptor();
        assertEquals(Order.HIGHEST.getOrder(), interceptor.order());
    }
}
