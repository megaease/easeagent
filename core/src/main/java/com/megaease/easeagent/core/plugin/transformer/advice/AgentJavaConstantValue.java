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

package com.megaease.easeagent.core.plugin.transformer.advice;

import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.utility.JavaConstant;

public class AgentJavaConstantValue extends JavaConstantValue {
    private final MethodIdentityJavaConstant constant;
    private final int pointcutIndex;

    /**
     * Creates a constant pool value representing a {@link JavaConstant}.
     *
     * @param constant The instance to load onto the operand stack.
     */
    public AgentJavaConstantValue(MethodIdentityJavaConstant constant, int pointcutIndex) {
        super(constant);
        this.constant = constant;
        this.pointcutIndex = pointcutIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
        Integer index = (Integer) constant.accept(Visitor.INSTANCE);
        methodVisitor.visitLdcInsn(index);
        return constant.getTypeDescription().getStackSize().toIncreasingSize();
    }

    public MethodIdentityJavaConstant getConstant() {
        return this.constant;
    }

    public int getPointcutIndex() {
        return this.pointcutIndex;
    }
}
