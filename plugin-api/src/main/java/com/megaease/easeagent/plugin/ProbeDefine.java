package com.megaease.easeagent.plugin;

import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;

/**
 * Pointcut can be defined by ProbeDefine implementation
 * and also can be defined through @OnClass and @OnMethod annotation
 */
@SuppressWarnings("unused")
public interface ProbeDefine extends Probe {
    /**
     * return the defined class matcher matching a class or a group of classes
     * eg.
     * ClassMatchers.hadInterface(A)
     *      .isPublic()
     *      .isAbstract()
     *      .or(ClassMatchers.hasSuperClass(B).isPublic())
     */
    ClassMatcher getClassMatcher();

    /**
     * return the defined method matcher
     * eg.
     * MethodMatchers.named("execute")
     *      .isPublic()
     *      .argNum(2)
     *      .arg(1, "java.lang.String")
     */
    MethodMatcher getMethodMatcher();

    /**
     * when return true, the transformer will add a Object field and a accessor
     */
    boolean isAddDynamicField();
}
