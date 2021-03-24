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

package com.megaease.easeagent.zipkin.http;

import brave.Tracing;
import brave.propagation.TraceContext;
import com.megaease.easeagent.common.HostAddress;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.function.Consumer;

public class HttpFilterLogInterceptor implements AgentInterceptor {


    private final Consumer<String> reportConsumer;

    public HttpFilterLogInterceptor(Consumer<String> reportConsumer) {
        this.reportConsumer = reportConsumer;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        Long beginTime = ContextUtils.getBeginTime(context);
        TraceContext traceContext = Tracing.current().currentTraceContext().get();

        RequestInfo requestInfo = RequestInfo.builder()
                .service("")
                .system("")
                .hostName(HostAddress.localhost())
                .hostIpv4(HostAddress.localaddr().getHostAddress())
                .category("application")
                .url(httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI())
                .method(httpServletRequest.getMethod())
                .headers(ServletUtils.getHeaders(httpServletRequest))
                .beginTime(beginTime)
                .queries(ServletUtils.getQueries(httpServletRequest))
                .clientIP(ServletUtils.getRemoteHost(httpServletRequest))
                .requestTime(System.currentTimeMillis())
                .beginCpuTime(System.nanoTime())
                .traceId(traceContext.traceIdString())
                .spanId(traceContext.spanIdString())
                .parentSpanId(traceContext.parentIdString())
                .build();

        // TODO: 2021/3/3 send info

        return chain.doAfter(methodInfo, context);
    }

}
