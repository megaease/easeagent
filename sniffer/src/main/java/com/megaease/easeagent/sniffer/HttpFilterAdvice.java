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
public abstract class HttpFilterAdvice implements Transformation {
    private static final String FILTER_NAME = "org.springframework.web.filter.CharacterEncodingFilter";
    static final String SERVLET_REQUEST = "javax.servlet.http.HttpServletRequest";
    static final String SERVLET_RESPONSE = "javax.servlet.http.HttpServletResponse";
    static final String FILTER_CHAIN = "javax.servlet.FilterChain";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(
                hasSuperType(named(FILTER_NAME)))
                .transform(doFilterInternal(
                        named("doFilterInternal").and(takesArguments(3))
                                .and(takesArgument(0, named(SERVLET_REQUEST)))
                                .and(takesArgument(1, named(SERVLET_RESPONSE)))
                                .and(takesArgument(2, named(FILTER_CHAIN)))
                        )
                ).end();
    }

    @AdviceTo(DoFilterInternal.class)
    protected abstract Definition.Transformer doFilterInternal(ElementMatcher<? super MethodDescription> matcher);

    static class DoFilterInternal extends AbstractAdvice {


        @Injection.Autowire
        DoFilterInternal(AgentInterceptorChainInvoker chainInvoker,
                         @Injection.Qualifier("supplier4Filter") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, chainInvoker,true);
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.Origin Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.Origin Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
}
