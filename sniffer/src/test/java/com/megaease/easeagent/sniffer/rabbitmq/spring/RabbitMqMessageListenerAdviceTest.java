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

package com.megaease.easeagent.sniffer.rabbitmq.spring;

import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.BaseSnifferTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class RabbitMqMessageListenerAdviceTest extends BaseSnifferTest {
    static List<Class<?>> classList;
    AgentInterceptorChainInvoker chainInvoker = spy(AgentInterceptorChainInvoker.getInstance());

    @Before
    public void before() {
        if (classList != null) {
            return;
        }
        Definition.Default def = new GenRabbitMqMessageListenerAdvice().define(Definition.Default.EMPTY);
        ClassLoader loader = this.getClass().getClassLoader();
        classList = Classes.transform(
                this.getClass().getName() + "$MyListener"
        )
                .with(def, new QualifiedBean("", chainInvoker),
                        new QualifiedBean("supplier4SpringRabbitMqMessageListenerOnMessage", this.mockSupplier())
                )
                .load(loader);
    }

    @Test
    public void invoke() throws Exception {
        MyListener myListener = (MyListener) classList.get(0).newInstance();
        MessageProperties messageProperties = mock(MessageProperties.class);
        Message message = new Message(new byte[0], messageProperties);
        List<Message> list = new ArrayList<>();
        list.add(message);
        myListener.onMessage(message);
        this.verifyInvokeTimes(this.chainInvoker, 1);
        myListener.onMessageBatch(list);
        this.verifyInvokeTimes(this.chainInvoker, 2);
    }

    static class MyListener implements MessageListener {

        @Override
        public void onMessage(Message message) {

        }

        @Override
        public void containerAckMode(AcknowledgeMode mode) {

        }

        @Override
        public void onMessageBatch(List<Message> messages) {

        }
    }
}
