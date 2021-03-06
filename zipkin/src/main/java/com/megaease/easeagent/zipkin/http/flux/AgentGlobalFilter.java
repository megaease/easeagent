package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

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
        Mono<Void> mono = chain.filter(exchange);
        return new AgentMono<>(mono, exchange, this.agentInterceptor);
    }

}
