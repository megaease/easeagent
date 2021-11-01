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

import com.megaease.easeagent.core.plugin.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.plugin.registry.QualifierRegistry;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.Dispatcher;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.OffsetMapping;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AdviceRegistry {
    private final static Logger log = LoggerFactory.getLogger(AdviceRegistry.class);
    static AtomicInteger index = new AtomicInteger(1);
    static Map<String, Integer> methodsSet = new ConcurrentHashMap<>();

    public static Integer check(TypeDescription instrumentedType,
                                MethodDescription instrumentedMethod,
                                Dispatcher.Resolved.ForMethodEnter methodEnter,
                                Dispatcher.Resolved.ForMethodExit methodExit) {
        String key = instrumentedType.getName() + ":" + instrumentedMethod.getDescriptor();
        Integer value = methodsSet.putIfAbsent(key, index.getAndIncrement());

        Integer pointcutIndex;
        if (value != null) {
            // merge
            pointcutIndex = getPointcutIndex(methodEnter);

            return 0;
        }
        value = methodsSet.get(key);
        pointcutIndex = updateStackManipulation(methodEnter, value);
        if (!pointcutIndex.equals(updateStackManipulation(methodExit, value))) {
            log.warn("If this occurs in production environment, there may have some issue!");
        }
        // dispatcher registry
        MethodTransformation methodTransformation = QualifierRegistry.getMethodTransformation(pointcutIndex);
        if (methodTransformation == null) {
            log.error("MethodTransformation get fail for {}", pointcutIndex);
            return 0;
        }
        AgentInterceptorChain chain = methodTransformation
            .getAgentInterceptorChain(Thread.currentThread().getContextClassLoader());

        // this advice have been register by other classloader, it return null
        if (com.megaease.easeagent.core.plugin.Dispatcher.register(value, chain) != null) {
            log.info("Advice has already registered, index {}", value);
        }

        return value;
    }

    static Integer getPointcutIndex(Dispatcher.Resolved resolved) {
        Integer index = 0;
        Map<Integer, OffsetMapping> enterMap = resolved.getOffsetMapping();
        for (Integer offset : enterMap.keySet()) {
            OffsetMapping om = enterMap.get(offset);
            if (!(om instanceof OffsetMapping.ForStackManipulation)) {
                continue;
            }
            OffsetMapping.ForStackManipulation forStackManipulation = (OffsetMapping.ForStackManipulation)om;
            if (!(forStackManipulation.getStackManipulation() instanceof AgentJavaConstantValue)) {
                continue;
            }

            AgentJavaConstantValue value = (AgentJavaConstantValue) forStackManipulation.getStackManipulation();
            index = value.getPointcutIndex();
            break;
        }
        return index;
    }

    static Integer updateStackManipulation(Dispatcher.Resolved resolved, Integer value) {
        int index = 0;
        Map<Integer, OffsetMapping> enterMap = resolved.getOffsetMapping();
        for (Integer offset : enterMap.keySet()) {
            OffsetMapping om = enterMap.get(offset);
            if (!(om instanceof OffsetMapping.ForStackManipulation)) {
                continue;
            }
            OffsetMapping.ForStackManipulation forStackManipulation = (OffsetMapping.ForStackManipulation)om;
            if (!(forStackManipulation.getStackManipulation() instanceof AgentJavaConstantValue)) {
                continue;
            }

            AgentJavaConstantValue oldValue = (AgentJavaConstantValue) forStackManipulation.getStackManipulation();
            index = oldValue.getPointcutIndex();

            MethodIdentityJavaConstant constant = new MethodIdentityJavaConstant(value);
            StackManipulation stackManipulation = new AgentJavaConstantValue(constant, index);
            enterMap.put(offset, forStackManipulation.with(stackManipulation));
            return index;
        }
        return index;
    }
}
