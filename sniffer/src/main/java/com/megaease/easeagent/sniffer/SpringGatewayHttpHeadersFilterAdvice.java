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
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

@Injection.Provider(Provider.class)
public abstract class SpringGatewayHttpHeadersFilterAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type((named("org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter")))
                .transform(filterRequest(named("filterRequest")
                )).end();
    }

    @AdviceTo(FilterRequest.class)
    abstract Definition.Transformer filterRequest(ElementMatcher<? super MethodDescription> matcher);

    static class FilterRequest {

        final ForwardLock lock;
        final AgentInterceptor agentInterceptor;

        @Injection.Autowire
        FilterRequest(@Injection.Qualifier("agentInterceptor4GatewayHeaders") AgentInterceptor agentInterceptor) {
            this.lock = new ForwardLock();
            this.agentInterceptor = agentInterceptor;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(@Advice.This(optional = true) Object invoker,
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
                  @Advice.This(optional = true) Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Exception exception) {
            release.apply(context -> agentInterceptor.after(invoker, method, args, null, exception, context));
        }
    }
}
