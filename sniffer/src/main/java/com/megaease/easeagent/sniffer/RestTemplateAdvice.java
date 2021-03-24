package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class RestTemplateAdvice implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.springframework.http.client.AbstractClientHttpRequest")))
                .transform(execute(named("executeInternal").and(takesArguments(1).and(takesArgument(0, named("org.springframework.http.HttpHeaders"))))))
                .end();
    }

    @AdviceTo(Execute.class)
    abstract Definition.Transformer execute(ElementMatcher<? super MethodDescription> matcher);


    static class Execute extends AbstractAdvice {

        @Injection.Autowire
        Execute(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                @Injection.Qualifier("supplier4RestTemplate") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, agentInterceptorChainInvoker, true);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Throwable throwable) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
}
