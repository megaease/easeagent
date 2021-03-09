package com.megaease.easeagent.core.interceptor;

import java.util.Map;

public interface AgentInterceptorChain {

    void doBefore(Object invoker, String method, Object[] args, Map<Object, Object> context);

    Object doAfter(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context);

    interface Builder {

        Builder addInterceptor(AgentInterceptor agentInterceptor);

        AgentInterceptorChain build();
    }
}
