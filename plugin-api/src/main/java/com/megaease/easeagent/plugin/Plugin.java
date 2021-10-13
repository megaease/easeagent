package com.megaease.easeagent.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * this annotation indicates the implementation of the AgentPlugin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Plugin {
    Class<?> value() default Object.class;
}
