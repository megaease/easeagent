package com.megaease.easeagent.zipkin.kafka.v2d3;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

public class KafkaTracingInterceptor implements AgentInterceptor {
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        System.out.println("\n\n\nbegin tracing kafka\n\n\n");
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        System.out.println("\n\n\nbegin tracing kafka\n\n\n");
        return chain.doAfter(methodInfo, context);
    }
}
