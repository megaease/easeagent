package com.megaease.easeagent.core.interceptor;

import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class AgentInterceptorChainInvoker {

    public static final AgentInterceptorChainInvoker instance = new AgentInterceptorChainInvoker();

    public static AgentInterceptorChainInvoker getInstance() {
        return instance;
    }

    public void doBefore(AgentInterceptorChain.Builder builder, Object invoker, String method, Object[] args, Map<Object, Object> context) {
        AgentInterceptorChain interceptorChain = builder.build();
        context.put(AgentInterceptorChain.class, interceptorChain);
        interceptorChain.doBefore(invoker, method, args, context);
    }

    public Object doAfter(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {
        AgentInterceptorChain interceptorChain = ContextUtils.getFromContext(context, AgentInterceptorChain.class);
        return interceptorChain.doAfter(invoker, method, args, retValue, throwable, context);
    }
}
