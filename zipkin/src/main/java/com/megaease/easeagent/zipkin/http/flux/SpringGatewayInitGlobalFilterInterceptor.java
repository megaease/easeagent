package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.springframework.cloud.gateway.filter.GlobalFilter;

import java.util.List;
import java.util.Map;

public class SpringGatewayInitGlobalFilterInterceptor implements AgentInterceptor {

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        List<GlobalFilter> list = null;
        if (method.equals("filteringWebHandler")) {
            list = (List<GlobalFilter>) args[0];
        }
        if (method.equals("gatewayControllerEndpoint")) {
            list = (List<GlobalFilter>) args[0];
        }
        if (method.equals("gatewayLegacyControllerEndpoint")) {
            list = (List<GlobalFilter>) args[1];
        }
        if (list == null) {
            return;
        }
        for (GlobalFilter globalFilter : list) {
            if (globalFilter instanceof AgentGlobalFilter) {
                return;
            }
        }
        list.add(0, new AgentGlobalFilter());
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {

    }
}
