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


    static class Execute {

        final ForwardLock lock;
        final AgentInterceptor agentInterceptor;

        @Injection.Autowire
        Execute(@Injection.Qualifier("agentInterceptor4RestTemplate") AgentInterceptor agentInterceptor) {
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