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
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

@AdviceTo(value = KafkaConsumerAdvice.class, qualifier = "poll", plugin = KafkaPlugin.class)
public class KafkaConsumerPollInterceptor implements NonReentrantInterceptor {
    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        if (consumerRecords == null || consumerRecords.isEmpty()) {
            return;
        }
        Consumer<?, ?> consumer = (Consumer<?, ?>) methodInfo.getInvoker();
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumer);
        for (ConsumerRecord<?, ?> consumerRecord : consumerRecords) {
            AgentDynamicFieldAccessor.setDynamicFieldValue(consumerRecord, uri);
        }
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
