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

package com.megaease.easeagent.sniffer.rabbitmq.v5.interceptor;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

import java.net.InetAddress;
import java.util.Map;

public class RabbitMqChannelPublishInterceptor implements AgentInterceptor {
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Channel channel = (Channel) methodInfo.getInvoker();
        AMQP.BasicProperties basicProperties = (AMQP.BasicProperties) methodInfo.getArgs()[4];
        if (basicProperties == null) {
            basicProperties = MessageProperties.MINIMAL_BASIC;
            methodInfo.getArgs()[4] = basicProperties;
        }
        Connection connection = channel.getConnection();
        InetAddress address = connection.getAddress();
        String hostAddress = address.getHostAddress();
        String uri = hostAddress + ":" + connection.getPort();
        context.put(ContextCons.MQ_URI, uri);
        chain.doBefore(methodInfo, context);
    }
}
