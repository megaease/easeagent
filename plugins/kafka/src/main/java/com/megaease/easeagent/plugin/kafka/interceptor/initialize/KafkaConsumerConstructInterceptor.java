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

package com.megaease.easeagent.plugin.kafka.interceptor.initialize;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaConsumerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.List;
import java.util.Map;

@AdviceTo(value = KafkaConsumerAdvice.class, qualifier = "constructor", plugin = KafkaPlugin.class)
public class KafkaConsumerConstructInterceptor implements NonReentrantInterceptor {

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Object invoker = methodInfo.getInvoker();
        Object configObj = methodInfo.getArgs()[0];
        String uri;
        if (configObj instanceof Map) {
            uri = KafkaUtils.getUri(((Map) configObj).get(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG));
        } else {
            ConsumerConfig config = (ConsumerConfig) methodInfo.getArgs()[0];
            List<String> list = config.getList(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
            uri = String.join(",", list);
        }
        AgentDynamicFieldAccessor.setDynamicFieldValue(invoker, uri);
    }

    @Override
    public int order() {
        return Order.TRACING_INIT.getOrder();
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }
}
