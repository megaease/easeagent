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
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.api.metric.name.MetricName;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqConsumerMetric;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqPlugin;
import com.megaease.easeagent.plugin.rabbitmq.spring.interceptor.RabbitMqOnMessageMetricInterceptor;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqConsumerAdvice;
import com.rabbitmq.client.Envelope;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@AdviceTo(value = RabbitMqConsumerAdvice.class, plugin = RabbitMqPlugin.class)
public class RabbitMqConsumerMetricInterceptor implements Interceptor {
    private static final Object START = new Object();
    private static volatile NameFactory NAME_FACTORY = null;
    private static volatile MetricRegistry METRIC = null;

    @Override
    @SuppressWarnings("all")
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        if (METRIC != null) {
            return;
        }
        synchronized (RabbitMqOnMessageMetricInterceptor.class) {
            if (METRIC != null) {
                return;
            }
            NAME_FACTORY = RabbitMqConsumerMetric.getNameFactory();
            Tags tags = new Tags("application", "rabbitmq-consumer", "resource");
            RedirectProcessor.setTagsIfRedirected(Redirect.RABBITMQ, tags);
            METRIC = EaseAgent.newMetricRegistry(config, NAME_FACTORY, tags);
        }
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        context.put(START, System.currentTimeMillis());
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Envelope envelope = (Envelope) methodInfo.getArgs()[1];
        metricAfter(envelope.getRoutingKey(), context.get(START), methodInfo.isSuccess());
    }

    private void metricAfter(String queue, long beginTime, boolean success) {
        Map<MetricSubType, MetricName> timerNames = NAME_FACTORY.timerNames(queue);
        MetricName metricName = timerNames.get(MetricSubType.DEFAULT);
        Timer timer = METRIC.timer(metricName.name());
        timer.update(System.currentTimeMillis() - beginTime, TimeUnit.MILLISECONDS);
        final Meter defaultMeter = METRIC.meter(NAME_FACTORY.meterName(queue, MetricSubType.CONSUMER));
        final Meter errorMeter = METRIC.meter(NAME_FACTORY.meterName(queue, MetricSubType.CONSUMER_ERROR));
        if (!success) {
            errorMeter.mark();
        }
        defaultMeter.mark();
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
