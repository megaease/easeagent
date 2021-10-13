/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.annotation;

import com.megaease.easeagent.plugin.asm.Modifier;
import com.megaease.easeagent.plugin.enums.ClassMatch;
import com.megaease.easeagent.plugin.enums.Operator;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(OnClasses.class)
public @interface OnClass {
    /**
     * match keyword
     */
    String name() default "";

    /**
     * MatchType
     * match the exact className when the type is 'MatchType.Named'
     * match the interface name when the type is 'MatchType.INTERFACE'
     * match the base name when the type is 'MatchType.Base'
     */
    ClassMatch classMatchType() default ClassMatch.NAMED;

    /**
     * filter the class by class modifier, ignore default
     */
    int modifier() default Modifier.ACC_NONE;

    /**
     * when true, except all match classes through above conditions
     */
    boolean negate() default false;

    /**
     * and / or operator, linking the next OnClass annotation
     * if this is the last OnClass annotation, it's ignored
     */
    Operator operator() default Operator.OR;
}
