package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Functions.compose;
import static com.google.common.collect.Iterators.transform;
import static com.hexdecteam.easeagent.ReduceF.reduce;
import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class TraceStack extends Transformation<TraceStack.Configuration> {

    private static final Function<String, Junction<TypeDescription>> NAME_START_WITHS =
            new Function<String, Junction<TypeDescription>>() {
                @Override
                public Junction<TypeDescription> apply(String input) {
                    return nameStartsWith(input);
                }
            };
    private static final Function<Junction<TypeDescription>, Junction<TypeDescription>> NOT =
            new Function<Junction<TypeDescription>, Junction<TypeDescription>>() {
                @Override
                public Junction<TypeDescription> apply(Junction<TypeDescription> input) {
                    return not(input);
                }
            };
    private static final ReduceF.BiFunction<Junction<TypeDescription>> OR =
            new ReduceF.BiFunction<Junction<TypeDescription>>() {
                @Override
                public Junction<TypeDescription> apply(Junction<TypeDescription> l, Junction<TypeDescription> r) {
                    return l.or(r);
                }
            };

    @Override
    protected Feature feature(final Configuration conf) {
        if (conf.include_class_prefix_list().isEmpty()) return Feature.NO_OP;
        final Iterator<String> includes = conf.include_class_prefix_list().iterator();
        final Iterator<String> excludes = conf.exclude_class_prefix_list().iterator();
        return new Feature() {
            @Override
            public Junction<TypeDescription> type() {
                if (!excludes.hasNext()) return reduce(transform(includes, NAME_START_WITHS), OR);
                return reduce(transform(includes, NAME_START_WITHS), OR)
                        .and(reduce(transform(excludes, compose(NOT, NAME_START_WITHS)), OR));
            }

            @Override
            public AgentBuilder.Transformer transformer() {
                return new AgentBuilder.Transformer() {
                    @Override
                    public Builder<?> transform(Builder<?> b, TypeDescription td, ClassLoader cld, JavaModule m) {
                        return b.visit(Advice.withCustomMapping()
                                             .bind(Signature.class, new SignatureForFixedValue())
                                             .to(FrameAdvice.class).on(not(
                                        isTypeInitializer().or(isSetter())
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
                                )));
                    }
                };
            }
        };
    }

    @ConfigurationDecorator.Binding("trace.stack")
    static abstract class Configuration {
        /**
         * The full name of class starts with the prefix in the list would not be traced.
         *
         * @return empty list as default.
         */
        List<String> exclude_class_prefix_list() { return Collections.emptyList();}

        /**
         * The full name of class starts with the prefix in the list would be traced.
         *
         * @return empty list as default.
         */
        List<String> include_class_prefix_list() { return Collections.emptyList();}

    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Signature {}

    static class FrameAdvice {
        @Advice.OnMethodEnter
        public static boolean enter(@Signature String signature) {
            return StackFrame.fork(signature);
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Enter boolean forked) {
            if (forked) StackFrame.join();
        }
    }

    static class SignatureForFixedValue extends Advice.DynamicValue.ForFixedValue<Signature> {

        @Override
        protected Object doResolve(TypeDescription it, MethodDescription im, ParameterDescription.InDefinedShape t,
                                   AnnotationDescription.Loadable<Signature> ad, Assigner as, boolean initialized) {
            return it.getSimpleName() + '#' + im.getName();
        }
    }
}
