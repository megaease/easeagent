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

package com.megaease.easeagent.sniffer.kafka.v2d3.interceptor;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;

public class KafkaConsumerPollInterceptor implements AgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Consumer<?, ?> consumer = (Consumer<?, ?>) methodInfo.getInvoker();
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumer);
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        if (consumerRecords != null) {
            for (ConsumerRecord<?, ?> consumerRecord : consumerRecords) {
                AgentDynamicFieldAccessor.setDynamicFieldValue(consumerRecord, uri);
            }
        }
        return chain.doAfter(methodInfo, context);
    }
}
