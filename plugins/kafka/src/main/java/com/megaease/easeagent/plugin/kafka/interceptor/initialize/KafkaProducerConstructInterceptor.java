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

package com.megaease.easeagent.plugin.kafka.interceptor.initialize;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaProducerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaUtils;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Map;

@AdviceTo(value = KafkaProducerAdvice.class, qualifier = "constructor", plugin = KafkaPlugin.class)
public class KafkaProducerConstructInterceptor implements NonReentrantInterceptor {

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Object invoker = methodInfo.getInvoker();
        String uri = getUri(methodInfo);
        AgentDynamicFieldAccessor.setDynamicFieldValue(invoker, uri);
    }

    private String getUri(MethodInfo methodInfo) {
        Object arg0 = methodInfo.getArgs()[0];
        ProducerConfig producerConfig = AgentFieldReflectAccessor.getFieldValue(methodInfo.getInvoker(), "producerConfig");
        if (producerConfig == null && arg0 instanceof ProducerConfig) {
            producerConfig = (ProducerConfig) arg0;
        }
        Object bootstrapServers = null;
        if (producerConfig != null) {
            bootstrapServers = producerConfig.getList(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        } else if (arg0 instanceof Map) {
            bootstrapServers = ((Map) arg0).get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        }
        if (bootstrapServers != null) {
            return KafkaUtils.getUri(bootstrapServers);
        }
        return null;
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
