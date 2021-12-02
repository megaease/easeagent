package easeagent.plugin.spring.gateway.reactor;

import com.megaease.easeagent.plugin.MethodInfo;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class AgentCoreSubscriber implements CoreSubscriber<Void> {

    private final CoreSubscriber<Void> actual;
    private final MethodInfo methodInfo;
    private final Object ctx;
    private final BiConsumer<MethodInfo, Object> finish;
    private final List<Void> results = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public AgentCoreSubscriber(CoreSubscriber<? super Void> actual,
                               MethodInfo methodInfo,
                               Object context,
                               BiConsumer<MethodInfo, Object> finish) {
        this.actual = (CoreSubscriber<Void>)actual;
        this.methodInfo = methodInfo;
        this.ctx = context;
        this.finish = finish;
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
    public void onNext(Void t) {
        actual.onNext(t);
        results.add(t);
    }

    @Override
    public void onError(Throwable t) {
        actual.onError(t);
        methodInfo.setThrowable(t);
        finish.accept(this.methodInfo, ctx);
        // EaseAgent.dispatcher.exit(chain, methodInfo, getContext(), results, t);
    }

    @Override
    public void onComplete() {
        actual.onComplete();
        methodInfo.setRetValue(results);
        finish.accept(this.methodInfo, ctx);
    }
}

