package com.megaease.easeagent.zipkin.http.flux;

import lombok.extern.slf4j.Slf4j;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class AgentMono<T> extends Mono<T> {

    private final Mono<T> monoObj;

    private final Map<Object, Object> context = new HashMap<>();

    public AgentMono(Mono<T> monoObj) {
        this.monoObj = monoObj;
    }

    public void addToContext(Object key, Object value) {
        context.put(key, value);
    }

    public Object getFromContext(Object key) {
        return context.get(key);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void subscribe(CoreSubscriber<? super T> actual) {
        log.info("begin subscribe");
        monoObj.subscribe(new AgentCoreSubscriber(this, actual));
    }

}
