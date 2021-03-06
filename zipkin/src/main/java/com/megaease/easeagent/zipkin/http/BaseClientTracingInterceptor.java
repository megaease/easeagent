package com.megaease.easeagent.zipkin.http;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;

import java.util.Map;

public abstract class BaseClientTracingInterceptor<Req, Resp> implements AgentInterceptor {

    protected final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;


    public BaseClientTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        Req request = getRequest(invoker, args);
        HttpClientRequest requestWrapper = this.buildHttpClientRequest(request);
        Span span = clientHandler.handleSend(requestWrapper);
        context.put(Span.class, span);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        CurrentTraceContext.Scope newScope = currentTraceContext.newScope(span.context());
        context.put(CurrentTraceContext.Scope.class, newScope);
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {
        try (CurrentTraceContext.Scope ignored = (CurrentTraceContext.Scope) context.get(CurrentTraceContext.Scope.class)) {
            Resp response = this.getResponse(invoker, args, retValue);
            Span span = (Span) context.get(Span.class);
            HttpClientResponse responseWrapper = this.buildHttpClientResponse(response);
            clientHandler.handleReceive(responseWrapper, span);
        }
    }

    public abstract Req getRequest(Object invoker, Object[] args);

    public abstract Resp getResponse(Object invoker, Object[] args, Object retValue);

    public abstract HttpClientRequest buildHttpClientRequest(Req req);

    public abstract HttpClientResponse buildHttpClientResponse(Resp resp);

}
