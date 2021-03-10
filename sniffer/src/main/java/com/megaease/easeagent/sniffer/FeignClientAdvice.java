package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class FeignClientAdvice implements Transformation {
    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("feign.Client")))
                .transform(execute(named("execute").and(takesArguments(2)
                        .and(takesArgument(0, named("feign.Request")))
                        .and(takesArgument(1, named("feign.Request$Options")))
                )))
                .end();
    }

    @AdviceTo(Execute.class)
    abstract Definition.Transformer execute(ElementMatcher<? super MethodDescription> matcher);


    static class Execute {

        final ForwardLock lock;
        final AgentInterceptorChain.Builder builder;
        final AgentInterceptorChainInvoker agentInterceptorChainInvoker;

        @Injection.Autowire
        Execute(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                @Injection.Qualifier("agentInterceptorChainBuilder4FeignClient") AgentInterceptorChain.Builder builder) {
            this.lock = new ForwardLock();
            this.builder = builder;
            this.agentInterceptorChainInvoker = agentInterceptorChainInvoker;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return lock.acquire(() -> {
                Map<Object, Object> context = ContextUtils.createContext();
                MethodInfo methodInfo = MethodInfo.builder()
                        .invoker(invoker)
                        .method(method)
                        .args(args)
                        .build();
                agentInterceptorChainInvoker.doBefore(this.builder, methodInfo, context);
                return context;
            });
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Exception exception) {
            release.apply(context -> {
                MethodInfo methodInfo = ContextUtils.getFromContext(context, MethodInfo.class);
                methodInfo.setThrowable(exception);
                agentInterceptorChainInvoker.doAfter(methodInfo, context);
            });
        }
    }
}
