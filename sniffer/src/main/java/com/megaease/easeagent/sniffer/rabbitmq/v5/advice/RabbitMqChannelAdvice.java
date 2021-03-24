package com.megaease.easeagent.sniffer.rabbitmq.v5.advice;

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
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class RabbitMqChannelAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type(hasSuperType(named("com.rabbitmq.client.Channel")).and(not(isInterface().or(isAbstract()))))
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .transform(doBasicPublish(named("basicPublish")
                        .and(takesArguments(6))
                        .and(isPublic()).and(returns(TypeDescription.VOID))))
                .transform(doBasicConsume(named("basicConsume")
                        .and(takesArguments(7))
                        .and(isPublic())))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);

    static class ObjConstruct extends AbstractAdvice {

        public ObjConstruct() {
            super(null, null);
        }

        @Advice.OnMethodExit
        public void exit() {

        }
    }

    @AdviceTo(DoBasicPublish.class)
    public abstract Definition.Transformer doBasicPublish(ElementMatcher<? super MethodDescription> matcher);

    static class DoBasicPublish extends AbstractAdvice {
        @Injection.Autowire
        public DoBasicPublish(@Injection.Qualifier("supplier4RabbitMqBasicPublish") Supplier<AgentInterceptorChain.Builder> supplier,
                              AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
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
                         @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }

    @AdviceTo(DoBasicConsume.class)
    public abstract Definition.Transformer doBasicConsume(ElementMatcher<? super MethodDescription> matcher);

    static class DoBasicConsume extends AbstractAdvice {

        @Injection.Autowire
        public DoBasicConsume(@Injection.Qualifier("supplier4RabbitMqBasicConsume") Supplier<AgentInterceptorChain.Builder> supplier,
                              AgentInterceptorChainInvoker chainInvoker) {
            super(supplier, chainInvoker);
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
                         @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                         @Advice.Thrown Throwable throwable
        ) {
            this.doExitNoRetValue(release, invoker, method, args, throwable);
        }
    }
}
