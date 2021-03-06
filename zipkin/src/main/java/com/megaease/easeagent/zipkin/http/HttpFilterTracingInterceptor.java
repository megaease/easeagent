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
import com.megaease.easeagent.core.utils.ServletUtils;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HttpFilterTracingInterceptor implements AgentInterceptor {

    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;

    public HttpFilterTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        HttpServerRequest requestWrapper = HttpServletRequestWrapper.create(httpServletRequest);
        Span span = httpServerHandler.handleReceive(requestWrapper);
        context.put(Span.class, span);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        currentTraceContext.newScope(span.context());
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) args[1];
        ServletUtils.setHttpRouteAttribute(httpServletRequest);
        Span span = (Span) context.get(Span.class);
        HttpServerResponse responseWrapper = HttpServletResponseWrapper.create(httpServletRequest, httpServletResponse, throwable);
        span.tag("http.route", ServletUtils.getHttpRouteAttribute(httpServletRequest));
        httpServerHandler.handleSend(responseWrapper, span);
    }

}
