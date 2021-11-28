package com.megaease.easeagent.plugin.springweb.reactor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import org.reactivestreams.Subscription;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.CoreSubscriber;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class AgentCoreSubscriber implements CoreSubscriber<ClientResponse> {

    private final CoreSubscriber<ClientResponse> actual;
    private final MethodInfo methodInfo;
    private final Integer chain;
    private final AsyncContext asyncContext;
    private final List<ClientResponse> results = new ArrayList<>();

    private Context context;

    public AgentCoreSubscriber(CoreSubscriber<ClientResponse> actual, MethodInfo methodInfo, Integer chain, AsyncContext context) {
        this.actual = actual;
        this.methodInfo = methodInfo;
        this.chain = chain;
        this.asyncContext = context;
    }

    @Nonnull
    @Override
    public reactor.util.context.Context currentContext() {
        return actual.currentContext();
    }

    @Override
    public void onSubscribe(@Nonnull Subscription s) {
        actual.onSubscribe(s);
    }

    @Override
    public void onNext(ClientResponse t) {
        Scope s = asyncContext.importToCurr();
        Span sp = asyncContext.getTracer().nextSpan();

        InitializeContext ctx = (InitializeContext)getContext();
        ctx.pushRetBound();
        ctx.push(sp);
        results.add(t);
    }

    @Override
    public void onError(Throwable t) {
        actual.onError(t);
        methodInfo.setThrowable(t);
        Span sp = getContext().pop();
        sp.error(t);
        // EaseAgent.dispatcher.exit(chain, methodInfo, getContext(), results, t);
        // this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context, newInterceptorChain);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
        methodInfo.setRetValue(results);

        InitializeContext ctx = (InitializeContext)getContext();
        Span sp = ctx.pop();
        ctx.popToBound();
        ctx.popRetBound();

        sp.finish();
        // EaseAgent.dispatcher.exit(chain, methodInfo, getContext(), results, null);
        // this.chainInvoker.doAfter(this.chainBuilder, methodInfo, context, newInterceptorChain);
    }

    private Context getContext() {
        if (context == null) {
            context = asyncContext.getContext();
        }
        return context;
    }
}
