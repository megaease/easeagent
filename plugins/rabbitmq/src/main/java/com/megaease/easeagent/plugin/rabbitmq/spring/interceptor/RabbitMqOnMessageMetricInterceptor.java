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

package com.megaease.easeagent.plugin.rabbitmq.spring.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqConsumerMetric;
import com.megaease.easeagent.plugin.rabbitmq.spring.RabbitMqMessageListenerAdvice;
import org.springframework.amqp.core.Message;

import java.util.List;

@SuppressWarnings("unused")
@AdviceTo(RabbitMqMessageListenerAdvice.class)
public class RabbitMqOnMessageMetricInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(RabbitMqOnMessageMetricInterceptor.class);
    private static final String AFTER_MARK = RabbitMqOnMessageMetricInterceptor.class.getName() + "$AfterMark";
    private static final Object START = new Object();
    private static volatile RabbitMqConsumerMetric metric = null;

    @Override
    @SuppressWarnings("all")
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        metric = EaseAgent.getOrCreateServiceMetric(config, RabbitMqConsumerMetric.buildOnMessageTags(), RabbitMqConsumerMetric.SERVICE_METRIC_SUPPLIER);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        context.put(START, System.currentTimeMillis());
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        if (methodInfo.getArgs()[0] instanceof List) {
            List<Message> messageList = (List<Message>) methodInfo.getArgs()[0];
            for (Message message : messageList) {
                metric.metricAfter(message.getMessageProperties().getConsumerQueue(),
                    context.get(START), methodInfo.isSuccess());
            }
        } else {
            Message message = (Message) methodInfo.getArgs()[0];
            metric.metricAfter(message.getMessageProperties().getConsumerQueue(),
                context.get(START), methodInfo.isSuccess());
        }
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }
}
