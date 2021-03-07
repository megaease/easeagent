package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;

@Slf4j
public class AgentMono<T> extends Mono<T> {

    private final Mono<T> monoObj;

    private final AgentInterceptor agentInterceptor;

    private final ServerWebExchange exchange;

    private final Map<Object, Object> context;

    public AgentMono(Mono<T> monoObj, ServerWebExchange exchange, AgentInterceptor agentInterceptor, Map<Object, Object> context) {
        this.monoObj = monoObj;
        this.agentInterceptor = agentInterceptor;
        this.exchange = exchange;
        this.context = context;
    }

    public AgentInterceptor getAgentInterceptor() {
        return agentInterceptor;
    }

    public Map<Object, Object> getContext() {
        return context;
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
