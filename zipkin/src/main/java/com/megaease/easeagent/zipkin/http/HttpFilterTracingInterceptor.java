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
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import brave.servlet.HttpServletRequestWrapper;
import brave.servlet.HttpServletResponseWrapper;
import brave.servlet.internal.ServletRuntime;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class HttpFilterTracingInterceptor implements AgentInterceptor {

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;
    private static final String SCOPE_CONTEXT_KEY = HttpFilterTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private static final String SEND_HANDLED_KEY = "brave.servlet.TracingFilter$SendHandled";
    public static final String ENABLE_KEY = "observability.tracings.request.enabled";
    private final ServletRuntime servletRuntime = ServletRuntime.get();
    private final Config config;

    public HttpFilterTracingInterceptor(Tracing tracing, Config config) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
        this.config = config;
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            chain.doBefore(methodInfo, context);
            return;
        }
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        Span span = (Span) httpServletRequest.getAttribute(ContextCons.SPAN);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope;
        if (span != null) {
            newScope = currentTraceContext.maybeScope(span.context());
            context.put(SCOPE_CONTEXT_KEY, newScope);
            chain.doBefore(methodInfo, context);
            return;
        }

        HttpServerRequest requestWrapper = HttpServletRequestWrapper.create(httpServletRequest);
        span = httpServerHandler.handleReceive(requestWrapper);
        httpServletRequest.setAttribute(ContextCons.SPAN, span);
        context.put(ContextCons.SPAN, span);

        newScope = currentTraceContext.newScope(span.context());
        context.put(SCOPE_CONTEXT_KEY, newScope);

        SendHandled sendHandled = new SendHandled();
        httpServletRequest.setAttribute(SEND_HANDLED_KEY, sendHandled);
        context.put(SEND_HANDLED_KEY, sendHandled);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        CurrentTraceContext.Scope scope = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY);
        if (scope == null) {
            return chain.doAfter(methodInfo, context);
        }
        try {
            HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
            HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
            Span span = (Span) httpServletRequest.getAttribute(ContextCons.SPAN);
            HttpServletResponse response = this.servletRuntime.httpServletResponse(httpServletResponse);
            if (servletRuntime.isAsync(httpServletRequest)) {
                this.servletRuntime.handleAsync(this.httpServerHandler, httpServletRequest, response, span);
            } else {
                HttpServerResponse responseWrapper = HttpServletResponseWrapper.create(httpServletRequest, httpServletResponse, methodInfo.getThrowable());
                span.tag("http.route", ServletUtils.getHttpRouteAttributeFromRequest(httpServletRequest));
                httpServerHandler.handleSend(responseWrapper, span);
            }
            return chain.doAfter(methodInfo, context);
        } finally {
            scope.close();
        }
    }

    static final class SendHandled extends AtomicBoolean {
    }
}
