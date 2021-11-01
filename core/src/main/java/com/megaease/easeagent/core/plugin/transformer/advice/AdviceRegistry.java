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

import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.Dispatcher;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.OffsetMapping;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AdviceRegistry {
    static AtomicInteger index = new AtomicInteger(1);
    static Map<String, Integer> methodsSet = new ConcurrentHashMap();

    public static Integer check(TypeDescription instrumentedType,
                                MethodDescription instrumentedMethod,
                                Dispatcher.Resolved.ForMethodEnter methodEnter,
                                Dispatcher.Resolved.ForMethodExit methodExit) {
        String key = instrumentedType.getName() + ":" + instrumentedMethod.getDescriptor();
        Integer value = methodsSet.putIfAbsent(key, index.getAndIncrement());

        if (value != null) {
            return 0;
        }
        value = methodsSet.get(key);
        updateStackManipulation(methodEnter, value);
        updateStackManipulation(methodExit, value);

        return value;
    }

    static void updateStackManipulation(Dispatcher.Resolved resolved, Integer value) {
        Map<Integer, OffsetMapping> enterMap = resolved.getOffsetMapping();
        for (Integer offset : enterMap.keySet()) {
            OffsetMapping om = enterMap.get(offset);
            if (!(om instanceof OffsetMapping.ForStackManipulation)) {
                continue;
            }

            MethodIdentityJavaConstant constant = new MethodIdentityJavaConstant(value);
            StackManipulation stackManipulation = new AgentJavaConstantValue(constant);
            OffsetMapping.ForStackManipulation forStackManipulation = (OffsetMapping.ForStackManipulation)om;
            enterMap.put(offset, forStackManipulation.with(stackManipulation));
        }
        return;
    }
}
