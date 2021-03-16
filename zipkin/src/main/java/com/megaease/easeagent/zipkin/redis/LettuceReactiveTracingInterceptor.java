package com.megaease.easeagent.zipkin.redis;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

public class LettuceReactiveTracingInterceptor extends CommonLettuceTracingInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        return this.innerAfter(methodInfo, context, chain);
    }
}
