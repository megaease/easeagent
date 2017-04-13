package com.megaease.easeagent.core;

import com.google.common.base.Predicate;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;

public interface Injection {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    @interface Provider {
        Class<?> value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.CONSTRUCTOR)
    @interface Autowire {
        Predicate<Constructor<?>> AUTOWIRED_CONS = new Predicate<Constructor<?>>() {
            @Override
            public boolean apply(Constructor<?> input) {
                return input.getAnnotation(Autowire.class) != null;
            }
        };
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Bean {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.PARAMETER)
    @interface Qualifier {
        String value();
    }

}
