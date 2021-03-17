//package com.megaease.easeagent.sniffer.lettuce.v5.advice;
//
//import com.megaease.easeagent.common.ForwardLock;
//import com.megaease.easeagent.core.AdviceTo;
//import com.megaease.easeagent.core.Definition;
//import com.megaease.easeagent.core.Injection;
//import com.megaease.easeagent.core.Transformation;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
//import com.megaease.easeagent.core.interceptor.AgentInterceptorChainInvoker;
//import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
//import com.megaease.easeagent.sniffer.AbstractAdvice;
//import com.megaease.easeagent.sniffer.Provider;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.description.method.MethodDescription;
//import net.bytebuddy.implementation.bytecode.assign.Assigner;
//import net.bytebuddy.matcher.ElementMatcher;
//
//import java.util.Map;
//
//import static net.bytebuddy.matcher.ElementMatchers.*;
//
//@Injection.Provider(Provider.class)
//public abstract class RedisFutureAdvice implements Transformation {
//
//    @Override
//    public <T extends Definition> T define(Definition<T> def) {
//        return def
//                .type((hasSuperType(named("io.lettuce.core.RedisFuture"))
//                        ).and(not(isInterface().or(isAbstract())))
//                )
//                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
//                .transform(afterExecute(nameContainsIgnoreCase("apply")
//                        .or(nameContainsIgnoreCase("accept")
//                                .or(nameContainsIgnoreCase("run")))))
//                .end()
//                ;
//    }
//
//    @AdviceTo(ObjConstruct.class)
//    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);
//
//    @AdviceTo(ObjConstruct.class)
//    public abstract Definition.Transformer afterExecute(ElementMatcher<? super MethodDescription> matcher);
//
//
//    static class ObjConstruct extends AbstractAdvice {
//
//        ObjConstruct() {
//            super(null, null);
//        }
//
//        @Advice.OnMethodExit
//        public void exit() {
//
//        }
//    }
//
//    static class DoCommandWithReturn extends AbstractAdvice {
//
//        @Injection.Autowire
//        DoCommandWithReturn(@Injection.Qualifier("builder4RedisFuture") AgentInterceptorChain.Builder builder,
//                            AgentInterceptorChainInvoker chainInvoker) {
//            super(builder, chainInvoker);
//        }
//
//        @Advice.OnMethodExit(onThrowable = Throwable.class)
//        public void exit(@Advice.Enter ForwardLock.Release<Map<Object, Object>> release,
//                         @Advice.This Object invoker,
//                         @Advice.Origin("#m") String method,
//                         @Advice.AllArguments Object[] args,
//                         @Advice.Thrown Throwable throwable
//        ) {
//            this.doExit(release, invoker, method, args, null, throwable);
//        }
//    }
//
//}
