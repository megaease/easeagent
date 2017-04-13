package com.megaease.easeagent.gen;

import com.megaease.easeagent.core.Transformation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Assembly {
    Class<? extends Transformation>[] value();
}
