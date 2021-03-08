package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class AgentGlobalFilter implements GlobalFilter {

    private final AgentInterceptorChain.Builder agentInterceptorChainBuilder;

    public AgentGlobalFilter(AgentInterceptorChain.Builder agentInterceptorChainBuilder) {
        this.agentInterceptorChainBuilder = agentInterceptorChainBuilder;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Map<Object, Object> context = ContextUtils.createContext();
        AgentInterceptorChainInvoker.getInstance().doBefore(this.agentInterceptorChainBuilder, this, "filter", new Object[]{exchange}, context);
        AgentInterceptorChain agentInterceptorChain = ContextUtils.getFromContext(context, AgentInterceptorChain.class);
        Mono<Void> mono = chain.filter(exchange);
        return new AgentMono<>(mono, exchange, agentInterceptorChain, context);
    }

}
