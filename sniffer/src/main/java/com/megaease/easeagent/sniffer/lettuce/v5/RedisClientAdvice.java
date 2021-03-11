package com.megaease.easeagent.sniffer.lettuce.v5;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.utils.AgentFieldAccessor;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.lang.reflect.Field;
import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class RedisClientAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("io.lettuce.core.RedisClient")))
                .transform(connectSync(nameStartsWith("connect").and(returns(named("io.lettuce.core.api.StatefulRedisConnection")))))
                .end()
                ;
    }

    @AdviceTo(ConnectStatefulSync.class)
    public abstract Definition.Transformer connectSync(ElementMatcher<? super MethodDescription> matcher);

    static class ConnectStatefulSync extends AbstractAdvice {
        @Injection.Autowire
        public ConnectStatefulSync(@Injection.Qualifier("agentInterceptorChainBuilder4LettuceRedisClient") AgentInterceptorChain.Builder builder,
                                   AgentInterceptorChainInvoker agentInterceptorChainInvoker
        ) {
            super(builder, agentInterceptorChainInvoker);
        }

        @Advice.OnMethodEnter
        protected ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.innerEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                  @Advice.This Object invoker,
                  @Advice.Origin("#m") String method,
                  @Advice.AllArguments Object[] args,
                  @Advice.Return Object retValue,
                  @Advice.Thrown Throwable throwable
        ) {
            this.innerExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
