package com.megaease.easeagent.zipkin.servlet;

import brave.Span;
import brave.Tracing;
import brave.http.HttpServerHandler;
import brave.http.HttpServerRequest;
import brave.http.HttpServerResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import brave.servlet.HttpServletRequestWrapper;
import brave.servlet.HttpServletResponseWrapper;
import com.megaease.easeagent.common.ServletUtils;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class HttpFilterTracingInterceptor implements AgentInterceptor {

    private final HttpTracing httpTracing;
    private final HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler;

    public HttpFilterTracingInterceptor() {
        this.httpTracing = HttpTracing.create(Tracing.current());
        this.httpServerHandler = HttpServerHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];

        HttpServerHandler<HttpServerRequest, HttpServerResponse> httpServerHandler = HttpServerHandler.create(httpTracing);
        HttpServerRequest requestWrapper = HttpServletRequestWrapper.create(httpServletRequest);
        Span span = httpServerHandler.handleReceive(requestWrapper);
        context.put(Span.class, span);

        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        currentTraceContext.newScope(span.context());
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) args[0];
        HttpServletResponse httpServletResponse = (HttpServletResponse) args[1];
        ServletUtils.setHttpRouteAttribute(httpServletRequest);
        Span span = (Span) context.get(Span.class);
        HttpServerResponse responseWrapper = HttpServletResponseWrapper.create(httpServletRequest, httpServletResponse, exception);
        span.tag("http.route", ServletUtils.getHttpRouteAttribute(httpServletRequest));
        httpServerHandler.handleSend(responseWrapper, span);
    }

}
