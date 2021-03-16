package com.megaease.easeagent.core.interceptor;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.Map;

public class AgentMono<T> extends Mono<T> {

    private final Mono<T> source;
    private final MethodInfo methodInfo;
    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;
    private final Map<Object, Object> context;
    private final boolean newInterceptorChain;

    public AgentMono(Mono<T> source, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context) {
        this(source, methodInfo, chainBuilder, chainInvoker, context, false);
    }

    public AgentMono(Mono<T> source, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context, boolean newInterceptorChain) {
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
