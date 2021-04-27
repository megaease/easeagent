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

package com.megaease.easeagent.metrics.rabbitmq;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.amqp.core.Message;

import java.util.List;
import java.util.Map;

public class RabbitMqMessageListenerMetricInterceptor implements AgentInterceptor {

    private final RabbitMqConsumerMetric rabbitMqConsumerMetric;

    public RabbitMqMessageListenerMetricInterceptor(RabbitMqConsumerMetric rabbitMqConsumerMetric) {
        this.rabbitMqConsumerMetric = rabbitMqConsumerMetric;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (methodInfo.getArgs()[0] instanceof List) {
            List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
            for (Message message : messageList) {
                this.rabbitMqConsumerMetric.after(message.getMessageProperties().getConsumerQueue(), ContextUtils.getBeginTime(context), methodInfo.isSuccess());
            }
        } else {
            Message message = (Message) methodInfo.getArgs()[0];
            this.rabbitMqConsumerMetric.after(message.getMessageProperties().getConsumerQueue(), ContextUtils.getBeginTime(context), methodInfo.isSuccess());
        }
        return chain.doAfter(methodInfo, context);
    }
}
