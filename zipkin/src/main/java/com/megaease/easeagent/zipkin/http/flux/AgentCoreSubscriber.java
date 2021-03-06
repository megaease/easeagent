package com.megaease.easeagent.zipkin.http.flux;

import com.megaease.easeagent.core.utils.ContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.util.context.Context;

import javax.annotation.Nonnull;
import java.util.Map;

@Slf4j
public class AgentCoreSubscriber<T> implements CoreSubscriber<T> {

    private final CoreSubscriber<T> coreSubscriberObj;

    private final AgentMono<T> agentMono;

    private final Map<Object, Object> context;

    private final Object[] interceptorArgs;

    public AgentCoreSubscriber(AgentMono<T> agentMono, CoreSubscriber<T> coreSubscriberObj) {
        this.agentMono = agentMono;
        this.coreSubscriberObj = coreSubscriberObj;
        this.context = ContextUtils.createContext();
        this.interceptorArgs = new Object[]{agentMono.getExchange()};
    }

    @Override
    @Nonnull
    public Context currentContext() {
        return this.coreSubscriberObj.currentContext();
    }

    @Override
    public void onSubscribe(@Nonnull Subscription s) {
        log.info("begin onSubscribe");
        this.agentMono.getAgentInterceptor().before(this, null, this.interceptorArgs, this.context);
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
        this.agentMono.getAgentInterceptor().after(this, null, this.interceptorArgs, null, t, this.context);
    }

    @Override
    public void onComplete() {
        log.info("begin onComplete");
        coreSubscriberObj.onComplete();
        this.agentMono.getAgentInterceptor().after(this, null, this.interceptorArgs, null, null, this.context);
    }
}
