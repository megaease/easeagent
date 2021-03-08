package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.utils.ContextUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class SpringGatewayHttpHeadersFilterAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(named("org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter"))
                .transform(filterRequest(named("filterRequest")))
                .end();
    }

    @AdviceTo(FilterRequest.class)
    abstract Definition.Transformer filterRequest(ElementMatcher<? super MethodDescription> matcher);

    static class FilterRequest {

        final ForwardLock lock;
        final AgentInterceptorChain.Builder builder;
        final AgentInterceptorChainInvoker agentInterceptorChainInvoker;

        @Injection.Autowire
        FilterRequest(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                      @Injection.Qualifier("agentInterceptorChainBuilder4GatewayHeaders") AgentInterceptorChain.Builder builder) {
            this.lock = new ForwardLock();
            this.builder = builder;
            this.agentInterceptorChainInvoker = agentInterceptorChainInvoker;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This(optional = true) Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return lock.acquire(() -> {
                Map<Object, Object> context = ContextUtils.createContext();
                agentInterceptorChainInvoker.doBefore(this.builder, invoker, method, args, context);
                return context;
            });
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        Object exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                    @Advice.This(optional = true) Object invoker,
                    @Advice.Origin("#m") String method,
                    @Advice.AllArguments Object[] args,
                    @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                    @Advice.Thrown Throwable throwable) {
            AtomicReference<Object> tmpRet = new AtomicReference<>(retValue);
            release.apply(context -> {
                agentInterceptorChainInvoker.doAfter(invoker, method, args, retValue, throwable, context);
                Object newRetValue = ContextUtils.getRetValue(context);
                if (newRetValue != null) {
                    tmpRet.set(newRetValue);
                }
            });
            return tmpRet.get();
        }
    }
}
