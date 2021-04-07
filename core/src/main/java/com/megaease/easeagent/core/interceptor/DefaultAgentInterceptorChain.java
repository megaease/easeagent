package com.megaease.easeagent.core.interceptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultAgentInterceptorChain implements AgentInterceptorChain {

    private final List<AgentInterceptor> agentInterceptors;

    private int pos = 0;

    public DefaultAgentInterceptorChain(List<AgentInterceptor> agentInterceptors) {
        this.agentInterceptors = agentInterceptors;
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Map<Object, Object> context) {
        if (pos == this.agentInterceptors.size()) {
            return;
        }
        AgentInterceptor interceptor = this.agentInterceptors.get(pos++);
        interceptor.before(methodInfo, context, this);
    }

    @Override
    public Object doAfter(MethodInfo methodInfo, Map<Object, Object> context) {
        pos--;
        if (pos < 0) {
            return methodInfo.getRetValue();
        }
        AgentInterceptor interceptor = this.agentInterceptors.get(pos);
        return interceptor.after(methodInfo, context, this);
    }

    @Override
    public void skipBegin() {
        this.pos = this.agentInterceptors.size();
    }

    public static class Builder implements AgentInterceptorChain.Builder {

        private final List<AgentInterceptor> list = new ArrayList<>();

        @Override
        public AgentInterceptorChain.Builder addInterceptor(AgentInterceptor agentInterceptor) {
            list.add(agentInterceptor);
            return this;
        }

        @Override
        public DefaultAgentInterceptorChain build() {
            return new DefaultAgentInterceptorChain(this.list);
        }
    }

}
