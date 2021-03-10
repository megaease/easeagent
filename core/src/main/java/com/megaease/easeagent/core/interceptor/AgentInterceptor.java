package com.megaease.easeagent.core.interceptor;

import java.util.Map;

public interface AgentInterceptor {

    /**
     * @param methodInfo instrumented method info
     * @param context    Interceptor can pass data, method `after` of interceptor can receive context data
     * @param chain      The chain can invoke next interceptor
     */
    void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain);

    /**
     * @param methodInfo instrumented method info
     * @param context    Interceptor can pass data, method `after` of interceptor can receive context data
     * @param chain      The chain can invoke next interceptor
     * @return The return value can change instrumented method result
     */
    Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain);

}
