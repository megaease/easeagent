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
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
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

    private final ServletRuntime servletRuntime = ServletRuntime.get();

    public HttpFilterTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
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
        try (CurrentTraceContext.Scope ignored = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY)) {
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
        }
    }

    static final class SendHandled extends AtomicBoolean {
    }
}
