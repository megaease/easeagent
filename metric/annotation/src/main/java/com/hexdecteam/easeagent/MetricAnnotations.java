package com.hexdecteam.easeagent;

import com.codahale.metrics.annotation.*;
import com.google.auto.service.AutoService;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription.Loadable;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType.Builder;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatcher.Junction;
import net.bytebuddy.utility.JavaModule;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.FluentIterable.from;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static net.bytebuddy.matcher.ElementMatchers.*;

@AutoService(Plugin.class)
public class MetricAnnotations extends Transformation<MetricAnnotations.Configuration> {

    @SuppressWarnings("unchecked")
    static final List<AnnotationFeature> FEATURES = Arrays.asList(
            new AnnotationFeature(Counted.class, CountedAdvice.class),
            new AnnotationFeature(Metered.class, MeteredAdvice.class),
            new AnnotationFeature(Timed.class, TimedAdvice.class),
            new AnnotationFeature(ExceptionMetered.class, ExceptionMeteredAdvice.class),
            new AnnotationFeature(Gauge.class, GaugeAdvice.class) {
                @Override
                protected Junction<? super MethodDescription> adviceMethod() {
                    return isConstructor();
                }
            });


    @Override
    protected Feature feature(Configuration conf) {
        final List<String> list = conf.supported_annotations();

        if (list.isEmpty()) return Feature.NO_OP;

        return new Feature.Compound(from(FEATURES).filter(new Predicate<AnnotationFeature>() {
            @Override
            public boolean apply(AnnotationFeature input) {
                return list.contains(input.type.getSimpleName());
            }
        }).toList());
    }

    @ConfigurationDecorator.Binding("metric.annotation")
    static abstract class Configuration {

        List<String> supported_annotations() {
            return from(FEATURES)
                    .transform(new Function<AnnotationFeature, String>() {
                        @Override
                        public String apply(AnnotationFeature input) {
                            return input.type.getSimpleName();
                        }
                    }).toList();
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface MetricName {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Cause {}

    static class AnnotationFeature implements Feature {

        final Class<? extends Annotation> type;
        final Class<?>                    adviceClass;

        AnnotationFeature(Class<? extends Annotation> annotation, Class<?> adviceClass) {
            this.type = annotation;
            this.adviceClass = adviceClass;
        }

        @Override
        public final ElementMatcher.Junction<TypeDescription> type() {
            return declaresMethod(isAnnotatedWith(type));
        }

        @Override
        public final AgentBuilder.Transformer transformer() {
            return new AgentBuilder.Transformer() {
                @Override
                public Builder<?> transform(Builder<?> b, TypeDescription td, ClassLoader ld, JavaModule m) {
                    return b.visit(Advice.withCustomMapping()
                                         .bind(MetricName.class, new MetricNameForFixedValue(type))
                                         .bind(Cause.class, new CauseForMixedValue(type))
                                         .to(adviceClass).on(adviceMethod()));
                }
            };
        }

        protected Junction<? super MethodDescription> adviceMethod() {
            return isAnnotatedWith(type);
        }

    }

    static class MeteredAdvice {
        @Advice.OnMethodEnter
        public static void enter(@MetricName String name) {
            EventBus.publish(new MetricEvents.Mark(name));
        }
    }

    static class ExceptionMeteredAdvice {
        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@MetricName String name, @Cause Class<Throwable> c, @Advice.Thrown Throwable error) {
            if (c.isInstance(error))
                EventBus.publish(new MetricEvents.Mark(name + '.' + ExceptionMetered.DEFAULT_NAME_SUFFIX));
        }
    }

    static class CountedAdvice {
        @Advice.OnMethodEnter
        public static void enter(@MetricName String name) {
            EventBus.publish(new MetricEvents.Inc(name));
        }
    }

    static class TimedAdvice {
        @Advice.OnMethodEnter
        public static long enter() {
            return System.nanoTime();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class)
        public static void exit(@Advice.Enter long started, @MetricName String name) {
            EventBus.publish(new MetricEvents.Update(name, System.nanoTime() - started, NANOSECONDS));
        }
    }

    static class GaugeAdvice {
        @Advice.OnMethodExit
        public static void exit(@Advice.This Object self,
                                @Advice.Origin("#t") String type,
                                @Advice.Origin("#m") String method) {
            final Method[] methods = self.getClass().getMethods();
            for (final Method m : methods) {
                final Gauge gauge = m.getAnnotation(Gauge.class);
                if (gauge == null) return;
                final String naming = MetricNaming.name(type, method, gauge.name(), gauge.absolute());
                EventBus.publish(new MetricEvents.Register(naming, m, self));
            }
        }
    }

    static class MetricNameForFixedValue extends AnnotationForFixedValue<MetricName> {

        MetricNameForFixedValue(Class<? extends Annotation> type) {
            super(type);
        }

        @Override
        protected String doResolve(TypeDescription it, MethodDescription im, Loadable<? extends Annotation> ad) {
            return MetricNaming.name(it.getName(), im.getName(),
                                     property(ad, "name", String.class),
                                     property(ad, "absolute", Boolean.class));
        }

    }

    static class CauseForMixedValue extends AnnotationForFixedValue<Cause> {

        CauseForMixedValue(Class<? extends Annotation> type) {
            super(type);
        }

        @Override
        protected Object doResolve(TypeDescription it, MethodDescription im, Loadable<? extends Annotation> ad) {
            return property(ad, "cause", TypeDescription.ForLoadedType.class);
        }
    }
}
