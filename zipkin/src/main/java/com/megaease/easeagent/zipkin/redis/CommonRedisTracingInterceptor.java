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

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        this.finishTracing(context);
        return chain.doAfter(methodInfo, context);
    }

    protected Span startTracing(String name, String host, int port, String cmd, Map<Object, Object> context) {
        Tracer tracer = Tracing.currentTracer();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return null;
        }
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("redis");
        if (host != null) {
            span.remoteIpAndPort(host, port);
        }
        context.put(Span.class, span);
        if (cmd != null) {
            span.tag("redis.method", cmd);
        }
        return span;
    }

    protected void finishTracing(Map<Object, Object> context) {
        try {
            Span span = ContextUtils.getFromContext(context, Span.class);
            if (span == null) {
                return;
            }
            span.finish();
            context.remove(Span.class);
        } catch (Exception ignored) {
        }
    }
}
