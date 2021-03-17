//package com.megaease.easeagent.sniffer.lettuce.v5.advice;
//
//import com.megaease.easeagent.core.AdviceTo;
//import com.megaease.easeagent.core.Definition;
//import com.megaease.easeagent.core.Injection;
//import com.megaease.easeagent.core.Transformation;
//import com.megaease.easeagent.core.utils.AgentDynamicFieldAccessor;
//import com.megaease.easeagent.sniffer.AbstractAdvice;
//import com.megaease.easeagent.sniffer.Provider;
//import net.bytebuddy.asm.Advice;
//import net.bytebuddy.description.method.MethodDescription;
//import net.bytebuddy.matcher.ElementMatcher;
//
//import static net.bytebuddy.matcher.ElementMatchers.*;
//
//@Injection.Provider(Provider.class)
//public abstract class ConnectionFutureAdvice implements Transformation {
//
//    @Override
//    public <T extends Definition> T define(Definition<T> def) {
//        return def
//                .type((hasSuperType(named("io.lettuce.core.ConnectionFuture"))
//                        ).and(not(isInterface().or(isAbstract())))
//                )
//                .transform(objConstruct(none(), AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME))
//                .end()
//                ;
//    }
//
//    @AdviceTo(ObjConstruct.class)
//    public abstract Definition.Transformer objConstruct(ElementMatcher<? super MethodDescription> matcher, String fieldName);
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
//}
