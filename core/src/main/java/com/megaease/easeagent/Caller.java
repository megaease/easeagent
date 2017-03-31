package com.megaease.easeagent;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class Caller {

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Signature {}

    public static class Feature extends Transformation.Feature.Compoundable {

        private final Class<?> adviceClass;

        public Feature(ElementMatcher.Junction<TypeDescription> type) {
            this(type,SignatureAdvice.class);
        }

        public Feature(ElementMatcher.Junction<TypeDescription> type, Class<?> adviceClass) {
            super(type);
            this.adviceClass = adviceClass;
        }

        @Override
        protected DynamicType.Builder<?> config(DynamicType.Builder<?> b) {
            return b.visit(Advice.withCustomMapping()
                                 .bind(Signature.class, new SignatureValue())
                                 .to(adviceClass).on(method()));
        }

        private ElementMatcher.Junction<MethodDescription> method() {
            return not(isTypeInitializer().or(isSetter())
                                          .or(isGetter())
                                          .or(isConstructor())
                                          .or(isClone())
                                          .or(isEquals())
                                          .or(isHashCode())
                                          .or(isToString())
                                          .or(ElementMatchers.<MethodDescription>isSynthetic())
                                          .or(ElementMatchers.<MethodDescription>isBridge())
                                          .or(ElementMatchers.<MethodDescription>isAbstract())
                                          .or(ElementMatchers.<MethodDescription>isNative())
                                          .or(ElementMatchers.<MethodDescription>isStrict())
            );
        }


        static class SignatureValue extends Advice.DynamicValue.ForFixedValue<Signature> {

            @Override
            protected Object doResolve(TypeDescription it, MethodDescription im, ParameterDescription.InDefinedShape t,
                                       AnnotationDescription.Loadable<Signature> ad, Assigner as, boolean initialized) {
                return it.getSimpleName() + '#' + im.getName();
            }

        }
    }

    static class SignatureAdvice {
        @Advice.OnMethodEnter
        public static String enter(@Signature String signature) {
            final String parent = SignatureHolder.CALLER.get();
            SignatureHolder.CALLER.set(signature);
            return parent;
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Enter String parent) {
            SignatureHolder.CALLER.set(parent);
        }

    }
}
