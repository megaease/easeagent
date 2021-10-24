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

package com.megaease.easeagent.common.kafka;


import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.plugin.MethodInfo;
import org.apache.kafka.clients.producer.Callback;

import java.util.Map;

public class KafkaProducerDoSendInterceptor implements AgentInterceptor {

    private final AgentInterceptorChainInvoker chainInvoker;

    private final AgentInterceptorChain.Builder callBackChainBuilder;

    public KafkaProducerDoSendInterceptor(AgentInterceptorChainInvoker chainInvoker, AgentInterceptorChain.Builder callBackChainBuilder) {
        this.chainInvoker = chainInvoker;
        this.callBackChainBuilder = callBackChainBuilder;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object[] args = methodInfo.getArgs();
        if (args[1] == null) {
            AgentKafkaCallback agentKafkaCallback = new AgentKafkaCallback(null, callBackChainBuilder,
                chainInvoker, methodInfo, context, true);
            args[1] = agentKafkaCallback;
            chain.doBefore(methodInfo, context);
            return;
        }
        if (args[1] instanceof Callback) {
            Callback callback = (Callback) args[1];
            AgentKafkaCallback agentKafkaCallback = new AgentKafkaCallback(callback, callBackChainBuilder, chainInvoker,
                methodInfo, context, true);
            args[1] = agentKafkaCallback;
        }
        chain.doBefore(methodInfo, context);
    }
}
