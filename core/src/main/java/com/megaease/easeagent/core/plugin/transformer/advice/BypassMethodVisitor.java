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

package com.megaease.easeagent.core.plugin.transformer.advice;

import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.OffsetMapping;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.utility.OpenedClassReader;

import java.util.Map;

public class BypassMethodVisitor extends MethodVisitor {
    public BypassMethodVisitor(MethodVisitor visitor, Map<Integer, OffsetMapping> offsetMappings) {
        super(OpenedClassReader.ASM_API, visitor);

    }
}
