package com.megaease.easeagent.zipkin.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.common.LettuceUtils;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import io.lettuce.core.RedisURI;

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
        RedisURI redisURI = null;
        Object data = ContextUtils.getFromContext(context, ContextCons.CACHE_URI);
        if (LettuceUtils.checkRedisUriInfo(data)) {
            redisURI = LettuceUtils.getOneRedisURI(data);
        }
        String name = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("redis");
        if (redisURI != null) {
            span.remoteIpAndPort(redisURI.getHost(), redisURI.getPort());
        }
        String cmd = ContextUtils.getFromContext(context, ContextCons.CACHE_CMD);
        span.tag("redis.method", cmd);
        context.put(Span.class, span);
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
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
