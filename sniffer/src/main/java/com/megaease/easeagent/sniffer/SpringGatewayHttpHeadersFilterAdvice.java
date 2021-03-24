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
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

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

    static class FilterRequest extends AbstractAdvice {


        @Injection.Autowire
        FilterRequest(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                      @Injection.Qualifier("supplier4GatewayHeaders") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, agentInterceptorChainInvoker, true);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This(optional = true) Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        Object exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                    @Advice.This(optional = true) Object invoker,
                    @Advice.Origin("#m") String method,
                    @Advice.AllArguments Object[] args,
                    @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                    @Advice.Thrown Throwable throwable) {
            return this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
