package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

public class LettuceInjectInterceptor implements AgentInterceptor {

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        return chain.doAfter(methodInfo, context);
    }
}
