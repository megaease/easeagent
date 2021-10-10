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

package com.megaease.easeagent.plugin.api.annotation;

import com.megaease.easeagent.plugin.api.asm.Modifier;
import com.megaease.easeagent.plugin.api.enums.Operator;
import com.megaease.easeagent.plugin.api.enums.StringMatch;

import javax.lang.model.type.NullType;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(OnMethods.class)
public @interface OnMethod {
    /**
     * method name
     * if the name is null or empty string, then this filed is ignored
     */
    String name() default "";

    /**
     * name match method
     * when the name is null or empty string, this field is ignored
     */
    StringMatch nameMatchType() default StringMatch.EQUALS;

    /**
     * filter the class by class modifier, ignore default
     */
    int modifier() default Modifier.ACC_NONE;

    /**
     * if args equals NullType.class, it ignore the args value
     */
    Class[] args() default NullType.class;

    /**
     * returnType
     * if the value is NullType, it ignore the returnType
     */
    Class returnType() default NullType.class;

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
