/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.metirc;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqConsumerMetric;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqConsumerAdvice;
import com.rabbitmq.client.Envelope;

@AdviceTo(value = RabbitMqConsumerAdvice.class, plugin = RabbitMqPlugin.class)
public class RabbitMqConsumerMetricInterceptor implements Interceptor {
    private static final Object START = new Object();
    private static volatile RabbitMqConsumerMetric metric = null;

    @Override
    @SuppressWarnings("all")
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        metric = EaseAgent.getOrCreateServiceMetric(config, RabbitMqConsumerMetric.buildConsumerTags(), RabbitMqConsumerMetric.SERVICE_METRIC_SUPPLIER);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        context.put(START, System.currentTimeMillis());
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Envelope envelope = (Envelope) methodInfo.getArgs()[1];
        metric.metricAfter(envelope.getRoutingKey(), context.get(START), methodInfo.isSuccess());
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
