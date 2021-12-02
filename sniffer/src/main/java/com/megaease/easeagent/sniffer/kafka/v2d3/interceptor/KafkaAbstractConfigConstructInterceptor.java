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

import com.megaease.easeagent.core.MiddlewareConfigProcessor;
import com.megaease.easeagent.core.ResourceConfig;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;

import java.util.Map;
import java.util.Properties;

public class KafkaAbstractConfigConstructInterceptor implements AgentInterceptor {
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ResourceConfig cnf = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_KAFKA);
        if (cnf == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        if (methodInfo.getArgs()[0] instanceof Properties) {
            Properties properties = (Properties) methodInfo.getArgs()[0];
            properties.put("bootstrap.servers", cnf.getUris());
        } else if (methodInfo.getArgs()[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) methodInfo.getArgs()[0];
            map.put("bootstrap.servers", cnf.getUris());
        }
        AgentInterceptor.super.before(methodInfo, context, chain);
    }

}
