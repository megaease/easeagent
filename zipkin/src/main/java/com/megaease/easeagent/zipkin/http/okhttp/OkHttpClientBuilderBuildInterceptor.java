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

package com.megaease.easeagent.zipkin.http.okhttp;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.plugin.MethodInfo;
import okhttp3.OkHttpClient;

import java.util.Map;

public class OkHttpClientBuilderBuildInterceptor implements AgentInterceptor {
    private final InternalOkHttpInterceptor internalOkHttpInterceptor;

    public OkHttpClientBuilderBuildInterceptor(AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.internalOkHttpInterceptor = new InternalOkHttpInterceptor(chainBuilder, chainInvoker);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        OkHttpClient.Builder builder = (OkHttpClient.Builder) methodInfo.getInvoker();
        builder.addInterceptor(internalOkHttpInterceptor);
        AgentInterceptor.super.before(methodInfo, context, chain);
    }

}
