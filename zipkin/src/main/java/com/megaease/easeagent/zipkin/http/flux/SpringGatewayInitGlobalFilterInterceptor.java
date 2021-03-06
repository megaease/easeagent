package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.List;
import java.util.Map;

public class SpringGatewayInitGlobalFilterInterceptor implements AgentInterceptor {

    private final AgentInterceptor agentInterceptor;

    private boolean loadAgentFilter;

    public SpringGatewayInitGlobalFilterInterceptor(AgentInterceptor agentInterceptor) {
        this.agentInterceptor = agentInterceptor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
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
        list.add(0, new AgentGlobalFilter(agentInterceptor));
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Throwable throwable, Map<Object, Object> context) {

    }
}
