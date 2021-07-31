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

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public abstract class BaseClientTracingInterceptor<Req, Resp> implements AgentInterceptor {

    protected final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;
    private static final String SCOPE_CONTEXT_KEY = BaseClientTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    protected static final String SPAN_CONTEXT_KEY = BaseClientTracingInterceptor.class.getName() + "-Span";
    public static final String ENABLE_KEY = "observability.tracings.remoteInvoke.enabled";
    private final Config config;

    public BaseClientTracingInterceptor(Tracing tracing, Config config) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
        this.config = config;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            chain.doBefore(methodInfo, context);
            return;
        }
        Req request = getRequest(methodInfo.getInvoker(), methodInfo.getArgs());
        if (request == null) {
            chain.doBefore(methodInfo, context);
            return;
        }
        HttpClientRequest requestWrapper = this.buildHttpClientRequest(request);
        Span span = clientHandler.handleSend(requestWrapper);
        context.put(SPAN_CONTEXT_KEY, span);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        context.put(SCOPE_CONTEXT_KEY, newScope);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        CurrentTraceContext.Scope scope = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY);
        if (scope == null) {
            return chain.doAfter(methodInfo, context);
        }
        try {
            if (!methodInfo.isSuccess()) {
                Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
                if (span != null) {
                    span.abandon();
                }
            } else {
                Resp response = this.getResponse(methodInfo.getInvoker(), methodInfo.getArgs(), methodInfo.getRetValue());
                if (response == null) {
                    return chain.doAfter(methodInfo, context);
                }
                Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
                HttpClientResponse responseWrapper = this.buildHttpClientResponse(response);
                clientHandler.handleReceive(responseWrapper, span);
            }
            return chain.doAfter(methodInfo, context);
        } finally {
            scope.close();
        }
    }

    public abstract Req getRequest(Object invoker, Object[] args);

    public abstract Resp getResponse(Object invoker, Object[] args, Object retValue);

    public abstract HttpClientRequest buildHttpClientRequest(Req req);

    public abstract HttpClientResponse buildHttpClientResponse(Resp resp);

}
