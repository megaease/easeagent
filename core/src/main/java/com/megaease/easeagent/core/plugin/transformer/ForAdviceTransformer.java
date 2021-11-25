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

import com.megaease.easeagent.core.plugin.CommonInlineAdvice;
import com.megaease.easeagent.core.plugin.Dispatcher;
import com.megaease.easeagent.core.plugin.annotation.EaseAgentInstrumented;
import com.megaease.easeagent.core.plugin.annotation.Index;
import com.megaease.easeagent.core.plugin.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.plugin.transformer.advice.*;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.OffsetMapping;
import com.megaease.easeagent.core.plugin.transformer.classloader.CompoundClassloader;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;

import static net.bytebuddy.matcher.ElementMatchers.*;

public class ForAdviceTransformer  implements AgentBuilder.Transformer {
    private final Logger log = LoggerFactory.getLogger(ForAdviceTransformer.class);

    private final AgentForAdvice transformer;
    private final MethodTransformation methodTransformInfo;

    public ForAdviceTransformer(MethodTransformation methodTransformInfo) {
        this.methodTransformInfo = methodTransformInfo;
        /*
        new AgentBuilder.Transformer
            .ForAdvice(Advice.withCustomMapping().bind(Index.class, methodTransformInfo.getIndex()))
            .include(getClass().getClassLoader())
            .advice(methodTransformInfo.getMatcher().and(not(isAnnotatedWith(EaseAgentInstrumented.class))),
                CommonInlineAdvice.class.getCanonicalName());
        this.transformer = new AgentForAdvice(AgentAdvice.withCustomMapping()
            .bind(Index.class, methodTransformInfo.getIndex()))
            .include(getClass().getClassLoader())
            .advice(methodTransformInfo.getMatcher().and(not(isAnnotatedWith(EaseAgentInstrumented.class))),
                CommonInlineAdvice.class.getCanonicalName());
        */

        MethodIdentityJavaConstant value = new MethodIdentityJavaConstant(methodTransformInfo.getIndex());
        StackManipulation stackManipulation = new AgentJavaConstantValue(value, methodTransformInfo.getIndex());
        TypeDescription typeDescription = value.getTypeDescription();

        OffsetMapping.Factory<Index> factory = new OffsetMapping.ForStackManipulation.Factory<>(Index.class,
            stackManipulation,
            typeDescription.asGenericType());

        this.transformer = new AgentForAdvice(AgentAdvice.withCustomMapping()
            .bind(factory))
            .include(getClass().getClassLoader())
            .advice(methodTransformInfo.getMatcher(),
                CommonInlineAdvice.class.getCanonicalName());
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> b, TypeDescription td, ClassLoader cl, JavaModule m) {
        CompoundClassloader.compound(this.getClass().getClassLoader(), cl);
        AdviceRegistry.setCurrentClassLoader(cl);

        return transformer.transform(b, td, cl, m);
    }
}