package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;

@Slf4j
public class AgentMono<T> extends Mono<T> {

    private final Mono<T> monoObj;

    private final AgentInterceptor agentInterceptor;

    private final ServerWebExchange exchange;

    public AgentMono(Mono<T> monoObj, ServerWebExchange exchange, AgentInterceptor agentInterceptor) {
        this.monoObj = monoObj;
        this.agentInterceptor = agentInterceptor;
        this.exchange = exchange;
    }

    public AgentInterceptor getAgentInterceptor() {
        return agentInterceptor;
    }

    public ServerWebExchange getExchange() {
        return exchange;
    }

    @SuppressWarnings({"rawtypes", "unchecked", ""})
    @Override
    public void subscribe(@Nonnull CoreSubscriber<? super T> actual) {
        log.info("begin subscribe");
        monoObj.subscribe(new AgentCoreSubscriber(this, actual));
    }

}
