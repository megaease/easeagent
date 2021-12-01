package com.megaease.easeagent.plugin.spring.gateway.reactor;

import com.megaease.easeagent.plugin.MethodInfo;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

public class AgentMono extends Mono<Void> {
    private final Mono<Void> source;
    private final MethodInfo methodInfo;
    private final Object context;
    private final BiConsumer<MethodInfo, Object> finish;

    public AgentMono(Mono<Void> mono, MethodInfo methodInfo,
                     Object ctx, BiConsumer<MethodInfo, Object> consumer) {
        this.source = mono;
        this.methodInfo = methodInfo;
        this.context = ctx;
        this.finish = consumer;
    }

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        this.source.subscribe(new AgentCoreSubscriber(actual, methodInfo, context, finish));
    }
}
