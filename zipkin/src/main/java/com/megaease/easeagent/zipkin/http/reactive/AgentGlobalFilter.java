package com.megaease.easeagent.zipkin.http.reactive;

import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.AgentMono;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Map;

public class AgentGlobalFilter implements GlobalFilter {

    private final AgentInterceptorChain.Builder agentInterceptorChainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;

    public AgentGlobalFilter(AgentInterceptorChain.Builder agentInterceptorChainBuilder, AgentInterceptorChainInvoker chainInvoker) {
        this.agentInterceptorChainBuilder = agentInterceptorChainBuilder;
        this.chainInvoker = chainInvoker;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Map<Object, Object> context = ContextUtils.createContext();
        MethodInfo methodInfo = MethodInfo.builder().invoker(this).method("filter").args(new Object[]{exchange}).build();
        chainInvoker.doBefore(this.agentInterceptorChainBuilder, methodInfo, context);
        Mono<Void> mono = chain.filter(exchange);
        return new AgentMono<>(mono, methodInfo, this.agentInterceptorChainBuilder, chainInvoker, context);
    }

}
