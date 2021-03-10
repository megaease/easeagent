package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.common.HttpServletService;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;


@Injection.Provider(Provider.class)
public abstract class HttpServletAdvice extends HttpServletService {

    @Override
    @AdviceTo(Service.class)
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

    static class Service {

        private final ForwardLock lock;
        private final AgentInterceptorChain.Builder builder;
        final AgentInterceptorChainInvoker agentInterceptorChainInvoker;

        @Injection.Autowire
        Service(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
                @Injection.Qualifier("agentInterceptorChainBuilder4Filter") AgentInterceptorChain.Builder builder) {
            this.lock = new ForwardLock();
            this.builder = builder;
            this.agentInterceptorChainInvoker = agentInterceptorChainInvoker;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.Origin Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return lock.acquire(() -> {
                Map<Object, Object> map = ContextUtils.createContext();
                MethodInfo methodInfo = MethodInfo.builder()
                        .invoker(invoker)
                        .method(method)
                        .args(args)
                        .build();
                agentInterceptorChainInvoker.doBefore(this.builder, methodInfo, map);
                return map;
            });

        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.Origin Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Exception exception
        ) {
            release.apply(context -> {
                ContextUtils.setEndTime(context);
                MethodInfo methodInfo = ContextUtils.getFromContext(context, MethodInfo.class);
                methodInfo.setThrowable(exception);
                agentInterceptorChainInvoker.doAfter(methodInfo, context);
            });
        }
    }
}
