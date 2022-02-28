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

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqChannelPublishInterceptorTest {

    @Test
    public void before() throws UnknownHostException {
        RabbitMqChannelPublishInterceptor interceptor = new RabbitMqChannelPublishInterceptor();
        Channel channel = mock(Channel.class);
        Connection connection = mock(Connection.class);
        when(channel.getConnection()).thenReturn(connection);
        String host = "127.0.0.1";
        int port = 1111;
        InetAddress inetAddress = InetAddress.getLocalHost();
        when(connection.getAddress()).thenReturn(inetAddress);
        when(connection.getPort()).thenReturn(port);
        MethodInfo methodInfo = MethodInfo.builder().invoker(channel).args(new Object[]{null, null, null, null, null}).build();
        Context context = EaseAgent.getContext();
        interceptor.before(methodInfo, context);
        assertNotNull(methodInfo.getArgs()[4]);
        assertEquals(host + ":" + port, context.get(ContextCons.MQ_URI));
    }

    @Test
    public void order() {
        RabbitMqChannelPublishInterceptor interceptor = new RabbitMqChannelPublishInterceptor();
        assertEquals(Order.HIGHEST.getOrder(), interceptor.order());
    }
}
