package com.megaease.easeagent.core.interceptor;

import java.util.List;
import java.util.Map;

public class AgentListInterceptor implements AgentInterceptor {

    private final List<AgentInterceptor> agentInterceptors;

    public AgentListInterceptor(List<AgentInterceptor> agentInterceptors) {
        this.agentInterceptors = agentInterceptors;
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        this.agentInterceptors.forEach(interceptor -> interceptor.before(invoker, method, args, context));
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        this.agentInterceptors.forEach(interceptor -> interceptor.after(invoker, method, args, retValue, exception, context));
    }
}
