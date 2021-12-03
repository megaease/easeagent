/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqChannelAdvice;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;

import java.net.InetAddress;

@SuppressWarnings("unused")
@AdviceTo(value = RabbitMqChannelAdvice.class, qualifier = "basicConsume", plugin = RabbitMqPlugin.class)
public class RabbitMqChannelConsumeInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Channel channel = (Channel) methodInfo.getInvoker();
        Connection connection = channel.getConnection();
        InetAddress address = connection.getAddress();
        String hostAddress = address.getHostAddress();
        String uri = hostAddress + ":" + connection.getPort();
        Consumer consumer = (Consumer) methodInfo.getArgs()[6];
        AgentDynamicFieldAccessor.setDynamicFieldValue(consumer, uri);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {

    }

    @Override
    public int order() {
        return Order.HIGHEST.getOrder();
    }
}
