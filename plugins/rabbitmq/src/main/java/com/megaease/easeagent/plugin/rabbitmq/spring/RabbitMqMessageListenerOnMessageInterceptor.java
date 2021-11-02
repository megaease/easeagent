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

package com.megaease.easeagent.plugin.rabbitmq.spring;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.plugin.MethodInfo;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

@AdviceTo(RabbitMqMessageListenerAdvice.class)
public class RabbitMqMessageListenerOnMessageInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Message message;
        if (methodInfo.getArgs()[0] instanceof List) {
            List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
            message = messageList.get(0);
        } else {
            message = (Message) methodInfo.getArgs()[0];
        }
        String uri = message.getMessageProperties().getHeader(ContextCons.MQ_URI);
        // context.put(ContextCons.MQ_URI, uri);
    }
}
