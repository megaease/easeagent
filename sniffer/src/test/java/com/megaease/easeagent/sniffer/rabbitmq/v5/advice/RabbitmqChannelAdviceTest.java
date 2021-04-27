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

package com.megaease.easeagent.sniffer.rabbitmq.v5.advice;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

public class RabbitmqChannelAdviceTest extends BaseSnifferTest {

    protected static List<Class<?>> classList;
    protected static AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

    @Before
    public void before() throws Exception {
        reset(chainInvoker);
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenRabbitMqChannelAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                "com.megaease.easeagent.sniffer.rabbitmq.v5.advice.MyChannel"
        )
                .with(def,
                        new QualifiedBean("supplier4RabbitMqBasicPublish", this.mockSupplier()),
                        new QualifiedBean("supplier4RabbitMqBasicConsume", this.mockSupplier()),
                        new QualifiedBean("", chainInvoker))
                .load(loader);

    }


    @Test
    public void invokePublish() throws Exception {
        MyChannel channel = (MyChannel) classList.get(0).newInstance();
        channel.basicPublish("exchange", "routingKey", false, false, null, null);

        this.verifyInvokeTimes(chainInvoker, 1);

    }

    @Test
    public void invokeConsume() throws Exception {
        MyChannel channel = (MyChannel) classList.get(0).newInstance();
        channel.basicConsume("queue", false, "tag", false, false, mock(Map.class), new Consumer() {
            @Override
            public void handleConsumeOk(String consumerTag) {

            }

            @Override
            public void handleCancelOk(String consumerTag) {

            }

            @Override
            public void handleCancel(String consumerTag) throws IOException {

            }

            @Override
            public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {

            }

            @Override
            public void handleRecoverOk(String consumerTag) {

            }

            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {

            }
        });

        this.verifyInvokeTimes(chainInvoker, 1);

    }

}
