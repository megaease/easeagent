package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.List;
import java.util.Map;

public class SpringGatewayInitGlobalFilterInterceptor implements AgentInterceptor {

    private boolean loadAgentFilter;

    private final AgentInterceptorChain.Builder agentInterceptorChainBuilder;

    public SpringGatewayInitGlobalFilterInterceptor(AgentInterceptorChain.Builder agentInterceptorChainBuilder) {
        this.agentInterceptorChainBuilder = agentInterceptorChainBuilder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context, AgentInterceptorChain chain) {
        List<GlobalFilter> list = null;
        switch (method) {
            case "filteringWebHandler":
            case "gatewayControllerEndpoint":
                list = (List<GlobalFilter>) args[0];
                break;
            case "gatewayLegacyControllerEndpoint":
                list = (List<GlobalFilter>) args[1];
                break;
        }
        if (list == null) {
            return;
        }
        if (this.loadAgentFilter) {
            return;
        }
        list.add(0, new AgentGlobalFilter(agentInterceptorChainBuilder));
        this.loadAgentFilter = true;
        chain.doBefore(invoker, method, args, context);
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context, AgentInterceptorChain chain) {
        chain.doAfter(invoker, method, args, retValue, throwable, context);
    }
}
