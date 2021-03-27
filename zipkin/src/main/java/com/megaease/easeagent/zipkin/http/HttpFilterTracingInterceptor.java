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
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.core.utils.ServletUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HttpFilterTracingInterceptor implements AgentInterceptor {

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;
    private static final String SCOPE_CONTEXT_KEY = HttpFilterTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private static final String SPAN_CONTEXT_KEY = HttpFilterTracingInterceptor.class.getName() + "-Span";

    public HttpFilterTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
        HttpServerRequest requestWrapper = HttpServletRequestWrapper.create(httpServletRequest);
        Span span = httpServerHandler.handleReceive(requestWrapper);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        context.put(SPAN_CONTEXT_KEY, span);
        context.put(SCOPE_CONTEXT_KEY, newScope);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        try (CurrentTraceContext.Scope ignored = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY)) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) methodInfo.getArgs()[0];
            HttpServletResponse httpServletResponse = (HttpServletResponse) methodInfo.getArgs()[1];
            ServletUtils.setHttpRouteAttribute(httpServletRequest);
            Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
            HttpServerResponse responseWrapper = HttpServletResponseWrapper.create(httpServletRequest, httpServletResponse, methodInfo.getThrowable());
            span.tag("http.route", ServletUtils.getHttpRouteAttribute(httpServletRequest));
            httpServerHandler.handleSend(responseWrapper, span);
        }
        return chain.doAfter(methodInfo, context);
    }

}
