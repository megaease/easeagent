package com.megaease.easeagent.sniffer.kafka.v2d3.advice;

import brave.Tracing;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.AdviceTo;
import com.megaease.easeagent.core.Definition;
import com.megaease.easeagent.core.Injection;
import com.megaease.easeagent.core.Transformation;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
import com.megaease.easeagent.metrics.kafka.KafkaConsumerMetricInterceptor;
import com.megaease.easeagent.metrics.kafka.KafkaMetric;
import com.megaease.easeagent.sniffer.AbstractAdvice;
import com.megaease.easeagent.sniffer.Provider;
import com.megaease.easeagent.sniffer.kafka.v2d3.interceptor.KafkaConsumerConstructInterceptor;
import com.megaease.easeagent.zipkin.kafka.v2d3.KafkaConsumerTracingInterceptor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class KafkaConsumerAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def.type(hasSuperType(named("org.apache.kafka.clients.consumer.KafkaConsumer")
                .or(named("org.apache.kafka.clients.consumer.MockConsumer")))
                .or(named("org.apache.kafka.clients.consumer.KafkaConsumer"))
        )
                .transform(objConstruct(isConstructor().and(takesArguments(3))
                                .and(takesArgument(0, named("org.apache.kafka.clients.consumer.ConsumerConfig")))
                        , AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
                .transform(doPoll((named("poll")
                                .and(takesArguments(1)))
                                .and(takesArgument(0, named("java.time.Duration")))
                        )
                )
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);

    static class ObjConstruct extends AbstractAdvice {

        @Injection.Autowire
        public ObjConstruct(AgentInterceptorChainInvoker chainInvoker,
                            @Injection.Qualifier("supplier4KafkaConsumerConstructor") Supplier<AgentInterceptorChain.Builder> supplier) {
            super(supplier, chainInvoker, true);
        }

        @Advice.OnMethodExit
        public void exit(@Advice.This Object invoker,
                         @Advice.Origin("#m") String method,
                         @Advice.AllArguments Object[] args) {
            this.doConstructorExit(invoker, method, args);
        }
    }

    @AdviceTo(DoPoll.class)
    public abstract Definition.Transformer doPoll(ElementMatcher<? super MethodDescription> matcher);

    static class DoPoll extends AbstractAdvice {

        @Injection.Autowire
        public DoPoll(AgentInterceptorChainInvoker chainInvoker,
                      @Injection.Qualifier("supplier4KafkaConsumerDoPoll") Supplier<AgentInterceptorChain.Builder> supplier
        ) {
            super(supplier, chainInvoker, true);

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
        public Object exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
                           @Advice.This Object invoker,
                           @Advice.Origin("#m") String method,
                           @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
                           @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object retValue,
                           @Advice.Thrown Throwable throwable
        ) {
            return this.doExit(release, invoker, method, args, retValue, throwable);
        }
    }
}
