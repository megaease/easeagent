package easeagent.plugin.spring.gateway.reactor;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;

import java.util.function.BiConsumer;

public class AgentMono extends Mono<Void> {
    private final Mono<Void> source;
    private final MethodInfo methodInfo;
    // private final Object context;
    private final AsyncContext asyncContext;
    private final BiConsumer<MethodInfo, AsyncContext> finish;

    public AgentMono(Mono<Void> mono, MethodInfo methodInfo,
                     // Object ctx,
                     AsyncContext async,
                     BiConsumer<MethodInfo, AsyncContext> consumer) {
        this.source = mono;
        this.methodInfo = methodInfo;
        // this.context = ctx;
        this.finish = consumer;
        this.asyncContext = async;
    }

    @Override
    public void subscribe(CoreSubscriber<? super Void> actual) {
        this.source.subscribe(new AgentCoreSubscriber(actual, methodInfo,
            // context,
            asyncContext, finish));
    }
}
