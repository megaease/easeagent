package com.megaease.easeagent.sniffer;

import com.megaease.easeagent.common.HttpServletService;
import com.megaease.easeagent.core.Definition;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;


public abstract class HttpServletAdvice extends HttpServletService {

    @Override
    protected abstract Definition.Transformer service(ElementMatcher<? super MethodDescription> matcher);

//    static class Service extends AbstractAdvice {
//
//        @Injection.Autowire
//        Service(AgentInterceptorChainInvoker agentInterceptorChainInvoker,
//                @Injection.Qualifier("agentInterceptorChainBuilder4Filter") AgentInterceptorChain.Builder builder) {
//            super(builder, agentInterceptorChainInvoker);
//        }
//
//        @Advice.OnMethodEnter
//        ForwardLock.Release<Map<Object, Object>> enter(
//                @Advice.Origin Object invoker,
//                @Advice.Origin("#m") String method,
//                @Advice.AllArguments Object[] args
//        ) {
//            return this.doEnter(invoker, method, args);
//        }
//
//        @Advice.OnMethodExit(onThrowable = Throwable.class)
//        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
//                  @Advice.Origin Object invoker,
//                  @Advice.Origin("#m") String method,
//                  @Advice.AllArguments Object[] args,
//                  @Advice.Thrown Throwable throwable
//        ) {
//            this.doExitNoRetValue(release, invoker, method, args, throwable);
//        }
//    }
}
