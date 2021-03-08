package com.megaease.easeagent.core.interceptor;

import java.util.Map;

public interface AgentInterceptor {

    /**
     * instrumen
     *
     * @param invoker The this reference of the instrumented method
     * @param method  instrumented method name
     * @param args    The arguments of instrumented method. If no args exist,args=null
     * @param context Interceptor can pass data, method `after` of interceptor can receive context data
     */
    void before(Object invoker, String method, Object[] args, Map<Object, Object> context, AgentInterceptorChain chain);

    /**
     * instrumen
     *
     * @param invoker   The this reference of the instrumented method
     * @param method    instrumented method name
     * @param args      The arguments of instrumented method. If no args exist,args is null.
     * @param retValue  The return value of instrumented method
     * @param throwable Throwable is exist if method throws exception. Otherwise it is null.
     * @param context   Interceptor can pass data, method `after` of interceptor can receive context data
     */
    void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context, AgentInterceptorChain chain);

}
