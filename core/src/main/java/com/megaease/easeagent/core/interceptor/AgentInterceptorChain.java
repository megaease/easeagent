package com.megaease.easeagent.core.interceptor;

import java.util.Map;

public interface AgentInterceptorChain {

    void doBefore(MethodInfo methodInfo, Map<Object, Object> context);

    Object doAfter(MethodInfo methodInfo, Map<Object, Object> context);

    void skipBegin();

    interface Builder {

        Builder addInterceptor(AgentInterceptor agentInterceptor);

        AgentInterceptorChain build();
    }
}
