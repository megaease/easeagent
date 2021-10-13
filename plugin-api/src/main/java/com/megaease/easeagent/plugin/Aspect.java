package com.megaease.easeagent.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A pointcut is a node required to be enhance,
 * and this annotation used to indicate the class defined the pointcut,
 * processed by PluginProcessor to generate META-INF/service file
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Aspect {
}
