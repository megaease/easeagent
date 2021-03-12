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

import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class SpringGatewayInitGlobalFilterAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(named("org.springframework.cloud.gateway.config.GatewayAutoConfiguration"))
                .transform(initBeans(named("filteringWebHandler")
                        .or(named("gatewayControllerEndpoint"))
                        .or(named("gatewayLegacyControllerEndpoint"))
                )).end();
    }

    @AdviceTo(InitBeans.class)
    abstract Definition.Transformer initBeans(ElementMatcher<? super MethodDescription> matcher);

    static class InitBeans extends AbstractAdvice {


        @Injection.Autowire
        InitBeans(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                  @Injection.Qualifier("agentInterceptorChainBuilder4Gateway") AgentInterceptorChain.Builder builder) {
            super(builder, agentInterceptorChainInvoker);
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
