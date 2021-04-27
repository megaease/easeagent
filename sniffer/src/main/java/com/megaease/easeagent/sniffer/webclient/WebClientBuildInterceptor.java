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

package com.megaease.easeagent.sniffer.webclient;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

public class WebClientBuildInterceptor implements AgentInterceptor {

    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;

    public WebClientBuildInterceptor(AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        WebClient.Builder builder = (WebClient.Builder) methodInfo.getInvoker();
        builder.filter(new WebClientFilter(chainBuilder, chainInvoker));
        chain.doBefore(methodInfo, context);
    }
}
