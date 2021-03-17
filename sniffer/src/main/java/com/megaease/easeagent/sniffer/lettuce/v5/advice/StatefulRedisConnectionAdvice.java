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
import net.bytebuddy.matcher.ElementMatcher;

import java.util.Map;

import static net.bytebuddy.matcher.ElementMatchers.*;

@Injection.Provider(Provider.class)
public abstract class StatefulRedisConnectionAdvice implements Transformation {

    @Override
    public <T extends Definition> T define(Definition<T> def) {
        return def
                .type((hasSuperType(named("io.lettuce.core.api.StatefulRedisConnection")
                        .or(named("io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection"))
                ).and(not(isInterface().or(isAbstract())))))
                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
//                .transform(getCommands(named("async").or(named("reactive"))))
                .end()
                ;
    }

    @AdviceTo(ObjConstruct.class)
    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fileName);


    static class ObjConstruct extends AbstractAdvice {

        ObjConstruct() {
            super(null, null);
        }

        @Advice.OnMethodExit
        public void exit() {

        }
    }

//    @AdviceTo(GetCommands.class)
//    public abstract Definition.Transformer getCommands(ElementMatcher<? super MethodDescription> matcher);
//
//    static class GetCommands extends AbstractAdvice {
//
//        @Injection.Autowire
//        GetCommands(@Injection.Qualifier("builder4StatefulRedisConnection") AgentInterceptorChain.Builder builder,
//                    AgentInterceptorChainInvoker chainInvoker) {
//            super(builder, chainInvoker);
//        }
//
//        @Advice.OnMethodEnter
//        public ForwardLock.Release<Map<Object, Object>> enter(
//                @Advice.This Object invoker,
//                @Advice.Origin("#m") String method,
//                @Advice.AllArguments Object[] args
//        ) {
//            return this.doEnter(invoker, method, args);
//        }
//
//        @Advice.OnMethodExit(onThrowable = Throwable.class)
//        public void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
//                         @Advice.This Object invoker,
//                         @Advice.Origin("#m") String method,
//                         @Advice.AllArguments Object[] args,
//                         @Advice.Return Object retValue,
//                         @Advice.Thrown Throwable throwable
//        ) {
//            this.doExit(release, invoker, method, args, retValue, throwable);
//        }
//    }

}
