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
import com.megaease.easeagent.common.JsonUtil;
import com.megaease.easeagent.config.AutoRefreshConfigItem;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public abstract class HttpLogInterceptor implements AgentInterceptor {

    private final Consumer<String> reportConsumer;

    private final AutoRefreshConfigItem<String> serviceName;

    public HttpLogInterceptor(AutoRefreshConfigItem<String> serviceName, Consumer<String> reportConsumer) {
        this.serviceName = serviceName;
        this.reportConsumer = reportConsumer;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        AccessLogServerInfo serverInfo = this.serverInfo(methodInfo, context);
        Long beginTime = ContextUtils.getBeginTime(context);
        TraceContext traceContext = Tracing.current().currentTraceContext().get();
        RequestInfo requestInfo = new RequestInfo();
        requestInfo.setService(this.serviceName.getValue());
        requestInfo.setHostName(HostAddress.localhost());
        requestInfo.setHostIpv4(HostAddress.localaddr().getHostAddress());
        requestInfo.setUrl(serverInfo.getMethod() + " " + serverInfo.getRequestURI());
        requestInfo.setMethod(serverInfo.getMethod());
        requestInfo.setHeaders(serverInfo.findHeaders());
        requestInfo.setBeginTime(beginTime);
        requestInfo.setQueries(serverInfo.findQueries());
        requestInfo.setClientIP(serverInfo.getClientIP());
        requestInfo.setBeginCpuTime(System.nanoTime());
        requestInfo.setTraceId(traceContext.traceIdString());
        requestInfo.setSpanId(traceContext.spanIdString());
        requestInfo.setParentSpanId(traceContext.parentIdString());
        context.put(RequestInfo.class, requestInfo);
        context.put(ServletAccessLogServerInfo.class, serverInfo);
        chain.doBefore(methodInfo, context);
    }

    public abstract AccessLogServerInfo serverInfo(MethodInfo methodInfo, Map<Object, Object> context);

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Long beginTime = ContextUtils.getBeginTime(context);
        AccessLogServerInfo serverInfo = this.serverInfo(methodInfo, context);
        RequestInfo requestInfo = ContextUtils.getFromContext(context, RequestInfo.class);
        requestInfo.setStatusCode(serverInfo.getStatusCode());
        if (!methodInfo.isSuccess()) {
            requestInfo.setStatusCode("500");
        }
        requestInfo.setRequestTime(System.currentTimeMillis() - beginTime);
        requestInfo.setCpuElapsedTime(System.nanoTime() - requestInfo.getBeginCpuTime());
        requestInfo.setResponseSize(serverInfo.getResponseBufferSize());
        requestInfo.setMatchUrl(serverInfo.getMatchURL());
        requestInfo.setTimestamp(System.currentTimeMillis());
        List<RequestInfo> list = new ArrayList<>(1);
        list.add(requestInfo);
        String value = JsonUtil.toJson(list);
//        log.info("access-log: {}", value);
        reportConsumer.accept(value);
        return chain.doAfter(methodInfo, context);
    }

}
