package com.megaease.easeagent.core.interceptor;

import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class AgentInterceptorChainInvoker {

    public static final AgentInterceptorChainInvoker instance = new AgentInterceptorChainInvoker();

    public static AgentInterceptorChainInvoker getInstance() {
        return instance;
    }

    public void doBefore(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context) {
        AgentInterceptorChain interceptorChain = this.prepare(builder, context);
        interceptorChain.doBefore(methodInfo, context);
    }

    public Object doAfter(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context) {
        return doAfter(builder, methodInfo, context, false);
    }

    public Object doAfter(AgentInterceptorChain.Builder builder, MethodInfo methodInfo, Map<Object, Object> context, boolean newInterceptorChain) {
        if (newInterceptorChain) {
            context.remove(AgentInterceptorChain.class);
        }
        AgentInterceptorChain interceptorChain = ContextUtils.getFromContext(context, AgentInterceptorChain.class);
        if (interceptorChain == null) {
            interceptorChain = this.prepare(builder, context);
            if (interceptorChain == null) {
                return methodInfo.getRetValue();
            }
            interceptorChain.skipBegin();
        }
        return interceptorChain.doAfter(methodInfo, context);
    }

    private AgentInterceptorChain prepare(AgentInterceptorChain.Builder builder, Map<Object, Object> context) {
        if (builder == null) {
            return null;
        }
        AgentInterceptorChain interceptorChain = builder.build();
        context.put(AgentInterceptorChain.class, interceptorChain);
        return interceptorChain;
    }
}
