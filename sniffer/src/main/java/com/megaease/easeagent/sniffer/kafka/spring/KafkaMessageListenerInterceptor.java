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

package com.megaease.easeagent.sniffer.kafka.spring;

import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.Map;

public class KafkaMessageListenerInterceptor implements AgentInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumerRecord);
        context.put(ContextCons.MQ_URI, uri);
        chain.doBefore(methodInfo, context);
    }

}
