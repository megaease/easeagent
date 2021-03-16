package com.megaease.easeagent.zipkin.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.*;
import com.megaease.easeagent.core.utils.ContextUtils;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.RedisURI;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class CommonLettuceTracingInterceptor implements AgentInterceptor {

    private static final String CURRENT_SPAN = CommonLettuceTracingInterceptor.class.getName() + ".currentSpan";

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Tracer tracer = Tracing.currentTracer();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return;
        }
        context.put(CURRENT_SPAN, currentSpan);
        RedisURI redisURI = ContextUtils.getFromContext(context, RedisURI.class);
        if (redisURI == null) {
            return;
        }
        String name = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("redis");
        span.remoteIpAndPort(redisURI.getHost(), redisURI.getPort());
        span.tag("redis.method", name);
        context.put(Span.class, span);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Object retValue = methodInfo.getRetValue();
        if (retValue instanceof Flux || retValue instanceof Mono || retValue instanceof RedisFuture) {
            return chain.doAfter(methodInfo, context);
        }
        return this.innerAfter(methodInfo, context, chain);
    }

    protected Object innerAfter(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        try {
            Span span = ContextUtils.getFromContext(context, Span.class);
            if (span == null) {
                return chain.doAfter(methodInfo, context);
            }
            span.finish();
            context.remove(Span.class);
        } catch (Exception ignored) {
        }
        return chain.doAfter(methodInfo, context);
    }
}
