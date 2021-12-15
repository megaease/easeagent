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

package com.megaease.easeagent.zipkin.http.reactive;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.AgentMono;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class AgentGlobalFilter implements GlobalFilter {

    private final AgentInterceptorChain.Builder agentInterceptorChainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;

    public AgentGlobalFilter(AgentInterceptorChain.Builder agentInterceptorChainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.agentInterceptorChainBuilder = agentInterceptorChainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Map<Object, Object> context = ContextUtils.createContext();
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("filter").args(new Object[]{exchange}).build();
        chainInvoker.doBefore(this.agentInterceptorChainBuilder, methodInfo, context);
        Mono<Void> mono = chain.filter(exchange);
        return new AgentMono<>(mono, methodInfo, this.agentInterceptorChainBuilder, chainInvoker, context, false);
    }

}
