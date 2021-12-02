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

package com.megaease.easeagent.zipkin;

import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.reactive.AgentGlobalFilter;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayInitGlobalFilterInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpringGatewayInitGlobalFilterInterceptorTest extends BaseZipkinTest {

    @Test
    public void success() {
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build().tracer();

        AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance();
        AgentInterceptorChain.Builder headersChainBuilder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(Tracing.current()));

        SpringGatewayInitGlobalFilterInterceptor interceptor = new SpringGatewayInitGlobalFilterInterceptor(headersChainBuilder, chainInvoker);
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(interceptor);

        Map<Object, Object> context = ContextUtils.createContext();
        List<GlobalFilter> list = new ArrayList<>();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(null)
                .method("filteringWebHandler")
                .args(new Object[]{list})
                .retValue(null)
                .throwable(null)
                .build();

        chainInvoker.doBefore(builder, methodInfo, context);

        Assert.assertTrue(interceptor.isLoadAgentFilter());
        Assert.assertEquals(1, list.size());
        Assert.assertTrue(list.get(0) instanceof AgentGlobalFilter);

    }

    @Test
    public void fail() {
        Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build().tracer();

        AgentInterceptorChainInvoker chainInvoker = AgentInterceptorChainInvoker.getInstance();
        AgentInterceptorChain.Builder headersChainBuilder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(Tracing.current()));

        SpringGatewayInitGlobalFilterInterceptor interceptor = new SpringGatewayInitGlobalFilterInterceptor(headersChainBuilder, chainInvoker);
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(interceptor);

        Map<Object, Object> context = ContextUtils.createContext();
        List<GlobalFilter> list = new ArrayList<>();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(null)
                .method("filteringWebHandler00")
                .args(new Object[]{list})
                .retValue(null)
                .throwable(null)
                .build();

        chainInvoker.doBefore(builder, methodInfo, context);

        Assert.assertFalse(interceptor.isLoadAgentFilter());
        Assert.assertTrue(list.isEmpty());

    }
}
