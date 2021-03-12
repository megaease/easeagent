package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class AbstractRedisClientAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type((named("io.lettuce.core.RedisClient")))
                .transform(objConstructor(isConstructor()))
                .end()
                ;
    }

    @AdviceTo(ObjConstructor.class)
    public abstract Definition.Transformer objConstructor(ElementMatcher<? super MethodDescription> matcher);

    static class ObjConstructor extends AbstractAdvice {
        @Injection.Autowire
        public ObjConstructor(@Injection.Qualifier("agentInterceptorChainBuilder4LettuceRedisClient") AgentInterceptorChain.Builder builder,
                              AgentInterceptorChainInvoker agentInterceptorChainInvoker
        ) {
            super(builder, agentInterceptorChainInvoker);
        }

        //        @Advice.OnMethodEnter
        protected ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(null, invoker, method, args, throwable);
        }
    }
}
