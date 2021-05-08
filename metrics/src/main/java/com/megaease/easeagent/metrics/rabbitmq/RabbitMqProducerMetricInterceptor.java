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

import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class RabbitMqProducerMetricInterceptor implements AgentInterceptor {

    public static final String ENABLE_KEY = "observability.metrics.rabbit.enabled";

    private final RabbitMqProducerMetric rabbitMqProducerMetric;

    private final Config config;

    public RabbitMqProducerMetricInterceptor(RabbitMqProducerMetric rabbitMqProducerMetric, Config config) {
        this.rabbitMqProducerMetric = rabbitMqProducerMetric;
        this.config = config;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
            return chain.doAfter(methodInfo, context);
        }
        String exchange = (String) methodInfo.getArgs()[0];
        String routingKey = (String) methodInfo.getArgs()[1];
        this.rabbitMqProducerMetric.after(exchange, routingKey, ContextUtils.getBeginTime(context), methodInfo.isSuccess());
        return chain.doAfter(methodInfo, context);
    }
}
