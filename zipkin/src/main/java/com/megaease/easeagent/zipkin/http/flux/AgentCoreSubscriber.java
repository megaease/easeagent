package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

@Slf4j
public class AgentCoreSubscriber<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> coreSubscriberObj;

    private final AgentMono<T> agentMono;

    public AgentCoreSubscriber(AgentMono<T> agentMono, CoreSubscriber<T> coreSubscriberObj) {
        this.agentMono = agentMono;
        this.coreSubscriberObj = coreSubscriberObj;
    }

    @Override
    public Context currentContext() {
        return this.coreSubscriberObj.currentContext();
    }

    @Override
    public void onSubscribe(Subscription s) {
        log.info("begin onSubscribe");
        coreSubscriberObj.onSubscribe(s);
    }

    @Override
    public void onNext(T t) {
        log.info("begin onNext");
        coreSubscriberObj.onNext(t);
    }

    @Override
    public void onError(Throwable t) {
        log.info("begin onError");
        coreSubscriberObj.onError(t);
        AgentInterceptor agentInterceptor = this.agentMono.getAgentInterceptor();
        agentInterceptor.after(this.agentMono.getAgentInterceptor(), "filter", this.agentMono.getArgs(), this.agentMono, t, this.agentMono.getContext());
    }

    @Override
    public void onComplete() {
        log.info("begin onComplete");
        coreSubscriberObj.onComplete();
    }
}
