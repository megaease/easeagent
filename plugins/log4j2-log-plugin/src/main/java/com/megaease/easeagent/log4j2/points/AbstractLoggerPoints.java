/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.log4j2.points;

import com.megaease.easeagent.plugin.Points;
import com.megaease.easeagent.plugin.matcher.ClassMatcher;
import com.megaease.easeagent.plugin.matcher.IClassMatcher;
import com.megaease.easeagent.plugin.matcher.IMethodMatcher;
import com.megaease.easeagent.plugin.matcher.MethodMatcher;
import com.megaease.easeagent.plugin.matcher.loader.ClassLoaderMatcher;
import com.megaease.easeagent.plugin.matcher.loader.IClassLoaderMatcher;

import java.util.Set;

public class AbstractLoggerPoints implements Points {
    @Override
    public IClassMatcher getClassMatcher() {
        return ClassMatcher.builder()
            .hasSuperClass("org.apache.logging.log4j.spi.AbstractLogger")
            .build();
    }

    @Override
    public Set<IMethodMatcher> getMethodMatcher() {
    return MethodMatcher.builder()
        .named("log")
        .argsLength(6)
        .arg(0, "org.apache.logging.log4j.Level")
        .arg(1, "org.apache.logging.log4j.Marker")
        .arg(2, "java.lang.String")
        .arg(3, "java.lang.StackTraceElement")
        .arg(4, "org.apache.logging.log4j.message.Message")
        .arg(5, "java.lang.Throwable")
        .build()
        .toSet();
    }

    /**
     * Do not match classes loaded by Agent classloader
     */
    @Override
    public IClassLoaderMatcher getClassLoaderMatcher() {
        return ClassLoaderMatcher.AGENT.negate();
    }
}
