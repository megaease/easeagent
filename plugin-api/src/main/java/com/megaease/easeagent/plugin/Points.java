/*
 * Copyright (c) 2021 MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin;

import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.loader.ClassLoaderMatcher;
import com.megaease.easeagent.plugin.matcher.loader.IClassLoaderMatcher;

import java.util.Set;

/**
 * Pointcut can be defined by ProbeDefine implementation
 * and also can be defined through @OnClass and @OnMethod annotation
 */
public interface Points {
    /**
     * return the defined class matcher matching a class or a group of classes
     * eg.
     * ClassMatcher.builder()
     *      .hadInterface(A)
     *      .isPublic()
     *      .isAbstract()
     *      .or()
     *        .hasSuperClass(B)
     *        .isPublic()
     *        .build()
     */
    IClassMatcher getClassMatcher();

    /**
     * return the defined method matcher
     * eg.
     * MethodMatcher.builder().named("execute")
     *      .isPublic()
     *      .argNum(2)
     *      .arg(1, "java.lang.String")
     *      .build().toSet()
     * or
     * MethodMatcher.multiBuilder()
     *      .match(MethodMatcher.builder().named("<init>")
     *          .argsLength(3)
     *          .arg(0, "org.apache.kafka.clients.consumer.ConsumerConfig")
     *          .qualifier("constructor")
     *          .build())
     *      .match(MethodMatcher.builder().named("poll")
     *          .argsLength(1)
     *          .arg(0, "java.time.Duration")
     *          .qualifier("poll")
     *          .build())
     *      .build();
     */
    Set<IMethodMatcher> getMethodMatcher();

    /**
     * when return true, the transformer will add a Object field and a accessor
     * The dynamically added member can be accessed by AgentDynamicFieldAccessor:
     *
     * AgentDynamicFieldAccessor.setDynamicFieldValue(instance, value)
     * value = AgentDynamicFieldAccessor.getDynamicFieldValue(instance)
     */
    default boolean isAddDynamicField() {
        return false;
    }

    /**
     * Only match classes loaded by the ClassLoaderMatcher
     * default as all classloader
     *
     * @return classloader matcher
     */
    default IClassLoaderMatcher getClassLoaderMatcher() {
        return ClassLoaderMatcher.ALL;
    }
}
