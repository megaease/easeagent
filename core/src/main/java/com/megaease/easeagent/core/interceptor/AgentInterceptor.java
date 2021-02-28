package com.megaease.easeagent.core.interceptor;

import java.util.ArrayList;
import java.util.List;
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
    void before(Object invoker, String method, Object[] args, Map<Object, Object> context);

    /**
     * instrumen
     *
     * @param invoker   The this reference of the instrumented method
     * @param method    instrumented method name
     * @param args      The arguments of instrumented method. If no args exist,args is null.
     * @param retValue  The return value of instrumented method
     * @param exception Exception is exist if method throws exception. Otherwise it is null.
     * @param context   Interceptor can pass data, method `after` of interceptor can receive context data
     */
    void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context);

    class Builder {

        private final List<AgentInterceptor> list = new ArrayList<>();

        public Builder addInterceptor(AgentInterceptor agentInterceptor) {
            list.add(agentInterceptor);
            return this;
        }

        public AgentInterceptor build() {
            return new AgentListInterceptor(list);
        }
    }
}
