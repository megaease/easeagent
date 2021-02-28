package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.common.HttpServletService;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.HashMap;
import java.util.Map;


@Injection.Provider(Provider.class)
public abstract class HttpServletAdvice extends HttpServletService {

    @Override
    @AdviceTo(Service.class)
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

    static class Service {

        private final ForwardLock lock;
        private final AgentInterceptor agentInterceptor;

        @Injection.Autowire
        Service(@Injection.Qualifier("agentInterceptor4HttpServlet") AgentInterceptor agentInterceptor) {
            this.lock = new ForwardLock();
            this.agentInterceptor = agentInterceptor;
        }

        @Advice.OnMethodEnter
        ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.Origin Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return lock.acquire(() -> {
                Map<Object, Object> map = new HashMap<>();
                agentInterceptor.before(invoker, method, args, map);
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
            release.apply(map -> this.agentInterceptor.after(invoker, method, args, null, exception, map));
        }
    }
}
