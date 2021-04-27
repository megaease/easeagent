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
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.List;
import java.util.Map;

public class KafkaProducerConstructInterceptor implements AgentInterceptor {

    @SuppressWarnings("unchecked")
    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object invoker = methodInfo.getInvoker();
        Map<String, Object> configs = (Map<String, Object>) methodInfo.getArgs()[0];
        List<String> serverConfig = (List<String>) configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String uri = String.join(",", serverConfig);
        AgentDynamicFieldAccessor.setDynamicFieldValue(invoker, uri);
        return chain.doAfter(methodInfo, context);
    }
}
