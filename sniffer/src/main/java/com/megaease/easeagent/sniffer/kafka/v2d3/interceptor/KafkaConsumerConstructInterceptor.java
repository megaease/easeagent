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

import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import java.util.List;
import java.util.Map;

public class KafkaConsumerConstructInterceptor implements AgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object invoker = methodInfo.getInvoker();
        ConsumerConfig config = (ConsumerConfig) methodInfo.getArgs()[0];
        List<String> list = config.getList(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String uri = String.join(",", list);
        if (invoker instanceof DynamicFieldAccessor) {
            DynamicFieldAccessor fieldAccessor = (DynamicFieldAccessor) invoker;
            fieldAccessor.setEaseAgent$$DynamicField$$Data(uri);
        }
        return chain.doAfter(methodInfo, context);
    }
}
