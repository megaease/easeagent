package com.megaease.easeagent.sniffer.lettuce.v5.advice;

import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class RedisCommandsAdvice implements Transformation {


    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type(
                        (
//                        hasSuperType(named("io.lettuce.core.api.sync.RedisCommands"))
                                hasSuperType(named("io.lettuce.core.AbstractRedisAsyncCommands"))
//                                        .or(named("io.lettuce.core.AbstractRedisAsyncCommands"))
                                        .or(hasSuperType(named("io.lettuce.core.AbstractRedisReactiveCommands")))
//                                        .or(named("io.lettuce.core.AbstractRedisReactiveCommands"))
                        )
//                                .and(not(isInterface()))
                )
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .transform(doCommandWithReturn(isPublic().and(not(returns(TypeDescription.VOID)))
                        .and(not(isTypeInitializer()
                                .or(isSetter())
                                .or(isGetter())
                                .or(isConstructor())
                                .or(isClone())
                                .or(isEquals())
                                .or(isHashCode())
                                .or(isToString())
                                .or(isSynthetic())
                                .or(isBridge())
                                .or(isAbstract())
                                .or(isNative())
                                .or(isStrict())
                        ))
                ))
                .transform(doCommandWithoutReturn(isPublic().and(returns(TypeDescription.VOID))
                        .and(not(isTypeInitializer()
                                .or(isSetter())
                                .or(isGetter())
                                .or(isConstructor())
                                .or(isClone())
                                .or(isEquals())
                                .or(isHashCode())
                                .or(isToString())
                                .or(isSynthetic())
                                .or(isBridge())
                                .or(isAbstract())
                                .or(isNative())
                                .or(isStrict())
                        ))
                ))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);

    @AdviceTo(DoCommandWithoutReturn.class)
    public abstract Definition.Transformer doCommandWithoutReturn(ElementMatcher<? super MethodDescription> matcher);

    @AdviceTo(DoCommandWithReturn.class)
    public abstract Definition.Transformer doCommandWithReturn(ElementMatcher<? super MethodDescription> matcher);

    static class ObjConstruct extends AbstractAdvice {

        ObjConstruct() {
            super(null, null);
        }

        @Advice.OnMethodExit
        public void exit() {

        }
    }

    static class DoCommandWithoutReturn extends AbstractAdvice {

        @Injection.Autowire
        DoCommandWithoutReturn(@Injection.Qualifier("builder4LettuceDoCommand") AgentInterceptorChain.Builder builder,
                               AgentInterceptorChainInvoker chainInvoker) {
            super(builder, chainInvoker);
        }

        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                         @Advice.This Object invoker,
                         @Advice.Origin("#m") String method,
                         @Advice.AllArguments Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }

    static class DoCommandWithReturn extends AbstractAdvice {

        @Injection.Autowire
        DoCommandWithReturn(@Injection.Qualifier("builder4LettuceDoCommand") AgentInterceptorChain.Builder builder,
                            AgentInterceptorChainInvoker chainInvoker) {
            super(builder, chainInvoker);
        }

        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments Object[] args
        ) {
            return this.doEnter(invoker, method, args);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public Object exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                           @Advice.This Object invoker,
                           @Advice.Origin("#m") String method,
                           @Advice.AllArguments Object[] args,
                           @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                           @Advice.Thrown Throwable throwable
        ) {
            return this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
