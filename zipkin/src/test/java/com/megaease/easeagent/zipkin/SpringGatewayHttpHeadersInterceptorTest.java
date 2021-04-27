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

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.DefaultAgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.zipkin.http.reactive.GatewayCons;
import com.megaease.easeagent.zipkin.http.reactive.SpringGatewayHttpHeadersInterceptor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SpringGatewayHttpHeadersInterceptorTest extends BaseZipkinTest {
    @Test
    public void success() {
        Tracer tracer = Tracing.newBuilder()
                .currentTraceContext(currentTraceContext)
                .build().tracer();
        AgentInterceptorChain.Builder builder = new DefaultAgentInterceptorChain.Builder()
                .addInterceptor(new SpringGatewayHttpHeadersInterceptor(Tracing.current()));
        Span span = tracer.newTrace();

        Map<String, Object> attrMap = new HashMap<>();
        attrMap.put(GatewayCons.SPAN_KEY, span);

        ServerWebExchange exchange = mock(ServerWebExchange.class);
        ServerHttpRequest request = mock(ServerHttpRequest.class);

        when(exchange.getRequest()).thenReturn(request);
        when(exchange.getAttributes()).thenReturn(attrMap);
        when(exchange.getAttribute(any(String.class))).thenAnswer(invocation -> attrMap.get(invocation.getArgumentAt(0, String.class)));

        HttpHeaders httpHeaders = new HttpHeaders();
        when(request.getHeaders()).thenReturn(httpHeaders);


        Map<Object, Object> context = ContextUtils.createContext();

        MethodInfo methodInfo = MethodInfo.builder()
                .invoker(null)
                .method(null)
                .args(new Object[]{null, exchange})
                .retValue(null)
                .throwable(null)
                .build();

        AgentInterceptorChainInvoker.getInstance().doBefore(builder, methodInfo, context);

        methodInfo.setRetValue(httpHeaders);

        Object ret = AgentInterceptorChainInvoker.getInstance().doAfter(builder, methodInfo, context);
        span.finish();
        HttpHeaders retValue = (HttpHeaders) ret;
        Assert.assertNotNull(retValue);
    }
}
