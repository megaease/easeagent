package com.megaease.easeagent.core.interceptor;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;

import javax.annotation.Nonnull;
import java.util.Map;

public class AgentFlux<T> extends Flux<T> {

    private final Flux<T> source;
    private final MethodInfo methodInfo;
    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;
    private final boolean newInterceptorChain;
    private final Map<Object, Object> context;

    public AgentFlux(Flux<T> source, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context) {
        this(source, methodInfo, chainBuilder, chainInvoker, context, false);
    }

    public AgentFlux(Flux<T> source, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context, boolean newInterceptorChain) {
        this.source = source;
        this.methodInfo = methodInfo;
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
        this.context = context;
        this.newInterceptorChain = newInterceptorChain;
    }

    @Override
    public void subscribe(@Nonnull CoreSubscriber<? super T> actual) {
        this.source.subscribe(new AgentCoreSubscriber<>(actual, methodInfo, chainBuilder, chainInvoker, context, newInterceptorChain));
    }

}
