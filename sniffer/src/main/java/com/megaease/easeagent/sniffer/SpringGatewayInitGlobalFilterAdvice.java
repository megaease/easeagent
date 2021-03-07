package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.utils.ContextUtils;
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

    static class InitBeans {

        final ForwardLock lock;
        final AgentInterceptor agentInterceptor;

        @Injection.Autowire
        InitBeans(@Injection.Qualifier("agentInterceptor4Gateway") AgentInterceptor agentInterceptor) {
            this.lock = new ForwardLock();
            this.agentInterceptor = agentInterceptor;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This Object invoker,
                                                       @Advice.Origin("#m") String method,
                                                       @Advice.AllArguments Object[] args) {
            return lock.acquire(() -> {
                Map<Object, Object> context = ContextUtils.createContext();
                agentInterceptor.before(invoker, method, args, context);
                return context;
            });
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Exception exception) {
            release.apply(context -> agentInterceptor.after(invoker, method, args, null, exception, context));
        }
    }
}
