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
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class KafkaAbstractConfigConstructInterceptor implements AgentInterceptor {
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Map<String, Object> dataMap = MiddlewareConfigProcessor.INSTANCE.getData(MiddlewareConfigProcessor.ENV_KAFKA);
        if (dataMap == null) {
            AgentInterceptor.super.before(methodInfo, context, chain);
            return;
        }
        String host = (String) dataMap.get("host");
        Integer port = (Integer) dataMap.get("port");
        List<String> list = new ArrayList<>();
        list.add(host + ":" + port);
        if (methodInfo.getArgs()[0] instanceof Properties) {
            Properties properties = (Properties) methodInfo.getArgs()[0];
            properties.put("bootstrap.servers", list);
        } else if (methodInfo.getArgs()[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) methodInfo.getArgs()[0];
            map.put("bootstrap.servers", list);
        }
        AgentInterceptor.super.before(methodInfo, context, chain);
    }

}
