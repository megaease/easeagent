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

package com.megaease.easeagent.core.plugin.transformer;

import com.megaease.easeagent.core.plugin.transformer.DynamicFieldAdvice.DynamicClassInit;
import com.megaease.easeagent.core.plugin.transformer.DynamicFieldAdvice.DynamicInstanceInit;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

public class DynamicFieldTransformer implements AgentBuilder.Transformer {
    private String fieldName;
    private Class<?> accessor;
    private final AgentBuilder.Transformer.ForAdvice transformer;

    public DynamicFieldTransformer(String fieldName) {
        this(fieldName, DynamicFieldAccessor.class);
    }

    public DynamicFieldTransformer(String fieldName, Class<?> accessor) {
        this.fieldName = fieldName;
        this.accessor = accessor;
        this.transformer = new AgentBuilder.Transformer
            .ForAdvice(Advice.withCustomMapping())
            .include(getClass().getClassLoader())
            .advice(ElementMatchers.isTypeInitializer(), DynamicClassInit.class.getName())
            .advice(ElementMatchers.isConstructor(), DynamicInstanceInit.class.getName());
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                            TypeDescription td, ClassLoader cl, JavaModule m) {
        if (!td.isAssignableTo(DynamicFieldAccessor.class)) {
            if (this.fieldName != null) {
                b = b.defineField(this.fieldName, Object.class, Opcodes.ACC_PRIVATE)
                    .implement(this.accessor)
                    .intercept(FieldAccessor.ofField(this.fieldName));

            }
        }
        return b;
    }
}
