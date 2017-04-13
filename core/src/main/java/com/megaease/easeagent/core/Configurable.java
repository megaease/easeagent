package com.megaease.easeagent.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Configurable {
    String bind();

    @Retention(RetentionPolicy.CLASS)
    @Target(ElementType.METHOD)
    @interface Item {}
}
