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
    private static final String FILTER_NAME = "javax.servlet.Filter";
    private static final String HTTP_SERVLET_NAME = "javax.servlet.http.HttpServlet";
    static final String SERVLET_REQUEST = "javax.servlet.ServletRequest";
    static final String SERVLET_RESPONSE = "javax.servlet.ServletResponse";

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(
                hasSuperType(namedOneOf(FILTER_NAME, HTTP_SERVLET_NAME)))
                .transform(doFilterOrService(
                        namedOneOf("doFilter", "service")
                                .and(takesArgument(0, named(SERVLET_REQUEST)))
                                .and(takesArgument(1, named(SERVLET_RESPONSE)))
                        )
                ).end();
    }

    @AdviceTo(DoFilterOrService.class)
    protected abstract Definition.Transformer doFilterOrService(ElementMatcher<? super MethodDescription> matcher);

    static class DoFilterOrService extends AbstractAdvice {

        @Injection.Autowire
        DoFilterOrService(AgentInterceptorChainInvoker chainInvoker,
                          @Injection.Qualifier("supplier4Filter") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, chainInvoker);
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
