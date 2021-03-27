package com.megaease.easeagent.zipkin.http;

import brave.Span;
import brave.Tracing;
import brave.http.HttpClientHandler;
import brave.http.HttpClientRequest;
import brave.http.HttpClientResponse;
import brave.http.HttpTracing;
import brave.propagation.CurrentTraceContext;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public abstract class BaseClientTracingInterceptor<Req, Resp> implements AgentInterceptor {

    protected final HttpClientHandler<HttpClientRequest, HttpClientResponse> clientHandler;
    private static final String SCOPE_CONTEXT_KEY = BaseClientTracingInterceptor.class.getName() + "-Tracer.SpanInScope";
    private static final String SPAN_CONTEXT_KEY = BaseClientTracingInterceptor.class.getName() + "-Span";

    public BaseClientTracingInterceptor(Tracing tracing) {
        HttpTracing httpTracing = HttpTracing.create(tracing);
        this.clientHandler = HttpClientHandler.create(httpTracing);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Req request = getRequest(methodInfo.getInvoker(), methodInfo.getArgs());
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
        try (CurrentTraceContext.Scope ignored = ContextUtils.getFromContext(context, SCOPE_CONTEXT_KEY)) {
            Resp response = this.getResponse(methodInfo.getInvoker(), methodInfo.getArgs(), methodInfo.getRetValue());
            Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
            HttpClientResponse responseWrapper = this.buildHttpClientResponse(response);
            clientHandler.handleReceive(responseWrapper, span);
        }
        return chain.doAfter(methodInfo, context);
    }

    public abstract Req getRequest(Object invoker, Object[] args);

    public abstract Resp getResponse(Object invoker, Object[] args, Object retValue);

    public abstract HttpClientRequest buildHttpClientRequest(Req req);

    public abstract HttpClientResponse buildHttpClientResponse(Resp resp);

}
