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

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.Map;

public class AgentKafkaCallback implements Callback {

    private final Callback source;

    private final AgentInterceptorChain.Builder chainBuilder;

    private final AgentInterceptorChainInvoker chainInvoker;

    private final MethodInfo methodInfo;

    private final Map<Object, Object> context;

    private final boolean newInterceptorChain;

    public AgentKafkaCallback(Callback source, AgentInterceptorChain.Builder chainBuilder,
                              AgentInterceptorChainInvoker chainInvoker, MethodInfo methodInfo,
                              Map<Object, Object> context, boolean newInterceptorChain) {
        this.source = source;
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
        this.methodInfo = methodInfo;
        this.context = context;
        this.newInterceptorChain = newInterceptorChain;
    }

    @Override
    public void onCompletion(RecordMetadata metadata, Exception exception) {
        context.put(ContextCons.ASYNC_FLAG, true);
        this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context, newInterceptorChain);
        if (this.source != null) {
            this.source.onCompletion(metadata, exception);
        }
    }
}
