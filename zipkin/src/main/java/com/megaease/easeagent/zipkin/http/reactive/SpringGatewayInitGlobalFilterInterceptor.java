package com.megaease.easeagent.zipkin.http.reactive;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.List;
import java.util.Map;

public class SpringGatewayInitGlobalFilterInterceptor implements AgentInterceptor {

    private boolean loadAgentFilter;

    private final AgentInterceptorChain.Builder headersFilterChainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;

    public SpringGatewayInitGlobalFilterInterceptor(AgentInterceptorChain.Builder headersFilterChainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.headersFilterChainBuilder = headersFilterChainBuilder;
        this.chainInvoker = chainInvoker;
    }

    public boolean isLoadAgentFilter() {
        return loadAgentFilter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        List<GlobalFilter> list = null;
        switch (methodInfo.getMethod()) {
            case "filteringWebHandler":
            case "gatewayControllerEndpoint":
                list = (List<GlobalFilter>) methodInfo.getArgs()[0];
                break;
            case "gatewayLegacyControllerEndpoint":
                list = (List<GlobalFilter>) methodInfo.getArgs()[1];
                break;
        }
        if (list == null) {
            return;
        }
        if (this.loadAgentFilter) {
            return;
        }
        list.add(0, new AgentGlobalFilter(headersFilterChainBuilder, chainInvoker));
        this.loadAgentFilter = true;
        chain.doBefore(methodInfo, context);
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        return chain.doAfter(methodInfo, context);
    }
}
