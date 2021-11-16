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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.metirc;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqProducerMetric;
import com.megaease.easeagent.plugin.rabbitmq.spring.interceptor.RabbitMqOnMessageMetricInterceptor;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqChannelAdvice;

@SuppressWarnings("unused")
@AdviceTo(value = RabbitMqChannelAdvice.class, qualifier = "basicPublish")
public class RabbitMqProducerMetricInterceptor implements Interceptor {
    private static volatile RabbitMqProducerMetric METRIC = null;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        if (METRIC != null) {
            return;
        }
        synchronized (RabbitMqOnMessageMetricInterceptor.class) {
            if (METRIC != null) {
                return;
            }
            METRIC = new RabbitMqProducerMetric(config);
        }
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        String exchange = (String) methodInfo.getArgs()[0];
        String routingKey = (String) methodInfo.getArgs()[1];
        METRIC.metricAfter(exchange, routingKey, ContextUtils.getBeginTime(context), methodInfo.isSuccess());
    }

    @Override
    public String getName() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }
}
