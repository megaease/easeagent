package com.megaease.easeagent.zipkin.redis;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class CommonLettuceTracingInterceptor extends CommonRedisTracingInterceptor {

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String uri = ContextUtils.getFromContext(context, ContextCons.CACHE_URI);
        String name = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        String cmd = ContextUtils.getFromContext(context, ContextCons.CACHE_CMD);
        this.startTracing(name, uri, cmd, context);
        chain.doBefore(methodInfo, context);
    }

}
