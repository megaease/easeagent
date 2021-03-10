//package com.megaease.easeagent.zipkin.http.reactive;
//
//import lombok.extern.slf4j.Slf4j;
//import org.reactivestreams.Subscription;
//import reactor.core.CoreSubscriber;
//import reactor.util.context.Context;
//
//import javax.annotation.Nonnull;
//
//@Slf4j
//public class AgentCoreSubscriber<T> implements CoreSubscriber<T> {
//
//    private final CoreSubscriber<T> coreSubscriberObj;
//
//    private final AgentMono<T> agentMono;
//
//    public AgentCoreSubscriber(AgentMono<T> agentMono, CoreSubscriber<T> coreSubscriberObj) {
//        this.agentMono = agentMono;
//        this.coreSubscriberObj = coreSubscriberObj;
//    }
//
//    @Override
//    @Nonnull
//    public Context currentContext() {
//        return this.coreSubscriberObj.currentContext();
//    }
//
//    @Override
//    public void onSubscribe(@Nonnull Subscription s) {
//        log.info("begin onSubscribe");
//        coreSubscriberObj.onSubscribe(s);
//    }
//
//    @Override
//    public void onNext(T t) {
//        log.info("begin onNext");
//        coreSubscriberObj.onNext(t);
//    }
//
//    @Override
//    public void onError(Throwable t) {
//        log.info("begin onError");
//        coreSubscriberObj.onError(t);
//        this.agentMono.getAgentInterceptorChain().doAfter(this, null, new Object[]{this.agentMono.getExchange()}, null, t, this.agentMono.getContext());
//    }
//
//    @Override
//    public void onComplete() {
//        log.info("begin onComplete");
//        coreSubscriberObj.onComplete();
//        this.agentMono.getAgentInterceptorChain().doAfter(this, null, new Object[]{this.agentMono.getExchange()}, null, null, this.agentMono.getContext());
//    }
//}
