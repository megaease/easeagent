package com.megaease.easeagent.sniffer.rabbitmq.spring;

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
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.named;

@Injection.Provider(Provider.class)
public abstract class RabbitmqListenerAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.springframework.amqp.core.MessageListener")))
                .transform(onMessage((named("onMessage").or(named("onMessageBatch")))
                ))
                .end()
                ;
    }

    @AdviceTo(OnMessage.class)
    public abstract Definition.Transformer onMessage(ElementMatcher<? super MethodDescription> matcher);

    static class OnMessage extends AbstractAdvice {

        @Injection.Autowire
        public OnMessage(AgentInterceptorChainInvoker chainInvoker,
                         @Injection.Qualifier("supplier4SpringRabbitMqOnMessage") Supplier<AgentInterceptorChain.Builder> supplier
        ) {
            super(supplier, chainInvoker);
        }

        @Advice.OnMethodEnter
        public ForwardLock.Release<Map<Object, Object>> enter(
                @Advice.This Object invoker,
                @Advice.Origin("#m") String method,
                @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args
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
