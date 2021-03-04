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

public abstract class BaseClientTracingInterceptor<T, E> implements AgentInterceptor {

    protected final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;


    public BaseClientTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        T request = getRequest(invoker, args);
        HttpClientRequest requestWrapper = this.buildHttpClientRequest(request);
        Span span = clientHandler.handleSend(requestWrapper);
        context.put(Span.class, span);
        CurrentTraceContext currentTraceContext = Tracing.current().currentTraceContext();
        currentTraceContext.newScope(span.context());
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        E response = this.getResponse(invoker, args, retValue);
        Span span = (Span) context.get(Span.class);
        HttpClientResponse responseWrapper = this.buildHttpClientResponse(response);
        clientHandler.handleReceive(responseWrapper, span);
    }

    public abstract T getRequest(Object invoker, Object[] args);

    public abstract E getResponse(Object invoker, Object[] args, Object retValue);

    public abstract HttpClientRequest buildHttpClientRequest(T t);

    public abstract HttpClientResponse buildHttpClientResponse(E e);

}
