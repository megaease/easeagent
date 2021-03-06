package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AgentMono<T> extends Mono<T> {

    private final Mono<T> monoObj;

    private final Object invoker;

    private final Object[] args;

    private final AgentInterceptor agentInterceptor;

    private final Map<Object, Object> context;

    public AgentMono(Mono<T> monoObj, Object invoker, Object[] args, AgentInterceptor agentInterceptor, Map<Object, Object> context) {
        this.monoObj = monoObj;
        this.invoker = invoker;
        this.args = args;
        this.agentInterceptor = agentInterceptor;
        this.context = context;
    }

    public Object getInvoker() {
        return invoker;
    }

    public Object[] getArgs() {
        return args;
    }

    public AgentInterceptor getAgentInterceptor() {
        return agentInterceptor;
    }

    public Map<Object, Object> getContext() {
        return context;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        log.info("begin subscribe");
        monoObj.subscribe(new AgentCoreSubscriber(this, actual));
    }

}
