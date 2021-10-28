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

import com.megaease.easeagent.core.plugin.annotation.EaseAgentInstrumented;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

public class AnnotationTransformer implements AgentBuilder.Transformer {
    private final AsmVisitorWrapper visitor;
    private final MethodTransformation methodTransformInfo;

    public AnnotationTransformer(MethodTransformation info) {
        this.methodTransformInfo = info;
        MemberAttributeExtension.ForMethod mForMethod = new MemberAttributeExtension.ForMethod()
            .annotateMethod(AnnotationDescription.Latent.Builder
                .ofType(EaseAgentInstrumented.class)
                .define("value", info.getIndex())
                .build());
        this.visitor = new ForMethodDelegate(mForMethod)
            .on(methodTransformInfo.getMatcher());
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
        return builder.visit(this.visitor);
    }

    public static class ForMethodDelegate implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {
        private MemberAttributeExtension.ForMethod mForMethod;

        ForMethodDelegate(MemberAttributeExtension.ForMethod mForMethod) {
            this.mForMethod = mForMethod;
        }

        @Override
        public MethodVisitor wrap(TypeDescription instrumentedType,
                                  MethodDescription instrumentedMethod,
                                  MethodVisitor methodVisitor,
                                  Implementation.Context implementationContext,
                                  TypePool typePool,
                                  int writerFlags,
                                  int readerFlags) {
            // check and registry

            MethodVisitor visitor = this.mForMethod.wrap(instrumentedType, instrumentedMethod,
                methodVisitor, implementationContext, typePool, writerFlags, readerFlags);

            return visitor;
        }

        public AsmVisitorWrapper on(ElementMatcher<? super MethodDescription> matcher) {
            return new AsmVisitorWrapper.ForDeclaredMethods().invokable(matcher, this);
        }
    }
}
