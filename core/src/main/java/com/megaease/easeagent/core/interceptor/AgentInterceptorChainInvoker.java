package com.megaease.easeagent.core.interceptor;

import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class AgentInterceptorChainInvoker {

    public static final AgentInterceptorChainInvoker instance = new AgentInterceptorChainInvoker();

    public static AgentInterceptorChainInvoker getInstance() {
        return instance;
    }

    public void doBefore(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context) {
        AgentInterceptorChain interceptorChain = builder.build();
        context.put(AgentInterceptorChain.class, interceptorChain);
        context.put(MethodInfo.class, methodInfo);
        interceptorChain.doBefore(methodInfo, context);
    }

    public Object doAfter(MethodInfo methodInfo, Map<Object, Object> context) {
        AgentInterceptorChain interceptorChain = ContextUtils.getFromContext(context, AgentInterceptorChain.class);
        return interceptorChain.doAfter(methodInfo, context);
    }
}
