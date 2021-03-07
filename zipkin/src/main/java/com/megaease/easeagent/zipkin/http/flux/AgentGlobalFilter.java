package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class AgentGlobalFilter implements GlobalFilter {

    private final AgentInterceptor agentInterceptor;

    public AgentGlobalFilter(AgentInterceptor agentInterceptor) {
        this.agentInterceptor = agentInterceptor;
    }

    public AgentInterceptor getAgentInterceptor() {
        return agentInterceptor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Map<Object, Object> context = ContextUtils.createContext();
        agentInterceptor.before(this, "filter", new Object[]{exchange}, context);
        Mono<Void> mono = chain.filter(exchange);
        return new AgentMono<>(mono, exchange, this.agentInterceptor, context);
    }

}
