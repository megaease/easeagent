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
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class RabbitmqConsumerAdviceTest extends BaseSnifferTest {
    protected static List<Class<?>> classList;
    protected static AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

    @Before
    public void before() throws Exception {
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenRabbitMqConsumerAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                this.getClass().getName() + "$MyConsumer"
//                , "com.megaease.easeagent.common.rabbitmq.v5.AgentRabbitMqConsumer"
        )
                .with(def,
                        new QualifiedBean("supplier4RabbitMqHandleDelivery", this.mockSupplier()),
                        new QualifiedBean("", chainInvoker))
                .load(loader);

    }

//    @Test
//    public void invoke0() throws Exception {
//        AgentRabbitMqConsumer consumer = (AgentRabbitMqConsumer) classList.get(1)
//                .getConstructor(Consumer.class, Object.class).newInstance(mock(Consumer.class), "uri");
//        Assert.assertTrue(consumer instanceof DynamicFieldAccessor);
//        consumer.handleDelivery("tag", mock(Envelope.class), mock(AMQP.BasicProperties.class), new byte[0]);
//        this.verifyInvokeTimes(chainInvoker, 1);
//    }

    @Test
    public void invoke1() throws Exception {
        MyConsumer consumer = (MyConsumer) classList.get(0).newInstance();
        Assert.assertTrue(consumer instanceof DynamicFieldAccessor);
        consumer.handleDelivery("tag", mock(Envelope.class), mock(AMQP.BasicProperties.class), new byte[0]);
        this.verifyInvokeTimes(chainInvoker, 1);
    }

    static class MyConsumer implements Consumer {


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

    }

}
