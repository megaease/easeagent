package com.megaease.easeagent.zipkin.redis;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Currency;
import java.util.Map;

public class SpringRedisTracingInterceptor implements AgentInterceptor {

    private static final String CURRENT_SPAN = SpringRedisTracingInterceptor.class.getName() + ".currentSpan";

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Tracer tracer = Tracing.currentTracer();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            context.put(CURRENT_SPAN, currentSpan);
        }
        this.innerBefore(methodInfo, context, chain);
        chain.doBefore(methodInfo, context);
    }

    public void innerBefore(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Span currentSpan = ContextUtils.getFromContext(context, CURRENT_SPAN);
        if (currentSpan == null) {
            return;
        }
        String name = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {

        return chain.doAfter(methodInfo, context);
    }
}
