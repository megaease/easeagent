package com.megaease.easeagent.zipkin.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class CommonRedisTracingInterceptor implements AgentInterceptor {
    private static final String SPAN_CONTEXT_KEY = CommonRedisTracingInterceptor.class.getName() + "-Span";

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            Span span = ContextUtils.getFromContext(context, Span.class);
            span.error(methodInfo.getThrowable());
        }
        this.finishTracing(context);
        return chain.doAfter(methodInfo, context);
    }

    protected void startTracing(String name, String uri, String cmd, Map<Object, Object> context) {
        Tracer tracer = Tracing.currentTracer();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return;
        }
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("redis");
//        if (uri != null) {
//            // TODO: 2021/3/24 add remote host
//        }
        context.put(SPAN_CONTEXT_KEY, span);
        if (cmd != null) {
            span.tag("redis.method", cmd);
        }
    }

    protected void finishTracing(Map<Object, Object> context) {
        try {
            Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
            if (span == null) {
                return;
            }
            span.finish();
            context.remove(SPAN_CONTEXT_KEY);
        } catch (Exception ignored) {
        }
    }
}
