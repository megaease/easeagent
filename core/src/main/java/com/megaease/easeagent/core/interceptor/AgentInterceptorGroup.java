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

package com.megaease.easeagent.core.interceptor;

import java.util.List;
import java.util.Map;

public abstract class AgentInterceptorGroup implements AgentInterceptor {

    private final AgentInterceptorChainInvoker chainInvoker;

    private final AgentInterceptorChain.Builder chainBuilder;

    public AgentInterceptorGroup(List<AgentInterceptor> interceptors, AgentInterceptorChainInvoker chainInvoker) {
        this.chainInvoker = chainInvoker;
        this.chainBuilder = ChainBuilderFactory.DEFAULT.createBuilder();
        for (AgentInterceptor interceptor : interceptors) {
            chainBuilder.addInterceptor(interceptor);
        }
    }

    public abstract void before4Group(MethodInfo methodInfo, Map<Object, Object> context);

    public abstract void after4Group(MethodInfo methodInfo, Map<Object, Object> context);


    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        this.before4Group(methodInfo, context);
        AgentInterceptorChain.Builder builder = ChainBuilderFactory.DEFAULT.createBuilder();
        this.chainInvoker.doBefore(builder, methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        this.after4Group(methodInfo, context);
        return chainInvoker.doAfter(this.chainBuilder, methodInfo, context);
    }
}
