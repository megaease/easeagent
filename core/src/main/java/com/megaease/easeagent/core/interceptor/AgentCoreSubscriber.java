package com.megaease.easeagent.core.interceptor;

import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import javax.annotation.Nonnull;
import java.util.Map;

public class AgentCoreSubscriber<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> actual;
    private final MethodInfo methodInfo;
    private final AgentInterceptorChain.Builder chainBuilder;
    private final AgentInterceptorChainInvoker chainInvoker;
    private final Map<Object, Object> context;

    public AgentCoreSubscriber(CoreSubscriber<T> actual, MethodInfo methodInfo, AgentInterceptorChain.Builder chainBuilder, AgentInterceptorChainInvoker chainInvoker, Map<Object, Object> context) {
        this.actual = actual;
        this.methodInfo = methodInfo;
        this.chainBuilder = chainBuilder;
        this.chainInvoker = chainInvoker;
        this.context = context;
    }

    @Nonnull
    @Override
    public Context currentContext() {
        return actual.currentContext();
    }

    @Override
    public void onSubscribe(@Nonnull Subscription s) {
        actual.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        actual.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        actual.onError(t);
        methodInfo.setThrowable(t);
        this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
        this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context);
    }
}
