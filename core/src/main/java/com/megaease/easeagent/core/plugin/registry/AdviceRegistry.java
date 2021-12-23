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

package com.megaease.easeagent.core.plugin.registry;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.megaease.easeagent.plugin.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.plugin.matcher.MethodTransformation;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.Dispatcher;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentAdvice.OffsetMapping;
import com.megaease.easeagent.core.plugin.transformer.advice.AgentJavaConstantValue;
import com.megaease.easeagent.core.plugin.transformer.advice.MethodIdentityJavaConstant;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class AdviceRegistry {
    private final static ThreadLocal<WeakReference<ClassLoader>> currentClassLoader = new ThreadLocal<>();
    private final static Logger log = LoggerFactory.getLogger(AdviceRegistry.class);
    static Map<String, IdentityPointcuts> methodsSet = new ConcurrentHashMap<>();

    public static Integer check(TypeDescription instrumentedType,
                                MethodDescription instrumentedMethod,
                                Dispatcher.Resolved.ForMethodEnter methodEnter,
                                Dispatcher.Resolved.ForMethodExit methodExit) {
        String type = instrumentedType.getName();
        String method = instrumentedMethod.getName();
        String methodDescriptor = instrumentedMethod.getDescriptor();
        String key = type + ":" + method + methodDescriptor;
        IdentityPointcuts newIdentity = new IdentityPointcuts();
        IdentityPointcuts identityPointcuts = methodsSet.putIfAbsent(key, newIdentity);

        Integer pointcutIndex;
        boolean merge = false;

        // already exist
        if (identityPointcuts != null) {
            newIdentity.tryRelease();
            pointcutIndex = getPointcutIndex(methodEnter);
            // this pointcut's interceptors have injected into chain
            if (identityPointcuts.checkPointcutExist(pointcutIndex)) {
                if (identityPointcuts.checkClassloaderExist()) {
                    // don't need to instrumented again.
                    return 0;
                } else {
                    /*
                     * Although the interceptor of the pointcut has been injected,
                     * the method of this class owned by current loader has not been instrumented
                     */
                    updateStackManipulation(methodEnter, identityPointcuts.getIdentify());
                    updateStackManipulation(methodExit, identityPointcuts.getIdentify());
                    return identityPointcuts.getIdentify();
                }
            } else {
                // Orchestration
                merge = true;
            }
        } else {
            // new
            identityPointcuts = newIdentity;
            pointcutIndex = updateStackManipulation(methodEnter, identityPointcuts.getIdentify());
            updateStackManipulation(methodExit, identityPointcuts.getIdentify());
        }

        // merge or registry
        MethodTransformation methodTransformation = PluginRegistry.getMethodTransformation(pointcutIndex);
        if (methodTransformation == null) {
            log.error("MethodTransformation get fail for {}", pointcutIndex);
            return 0;
        }
        int identity = identityPointcuts.getIdentify();
        AgentInterceptorChain chain = methodTransformation
            .getAgentInterceptorChain(identity, type, method, methodDescriptor);

        try {
            identityPointcuts.lock();
            AgentInterceptorChain previousChain = com.megaease.easeagent.core.plugin.Dispatcher.getChain(identity);
            if (previousChain == null) {
                com.megaease.easeagent.core.plugin.Dispatcher.register(identity, chain);
            } else {
                chain.merge(previousChain);
                com.megaease.easeagent.core.plugin.Dispatcher.updateChain(identity, chain);
            }
        } finally {
            identityPointcuts.unlock();
        }

        if (merge) {
            return 0;
        }

        return identity;
    }

    static Integer getPointcutIndex(Dispatcher.Resolved resolved) {
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

    public static void setCurrentClassLoader(ClassLoader loader) {
        currentClassLoader.set(new WeakReference<>(loader));
    }

    public static ClassLoader getCurrentClassLoader() {
        return currentClassLoader.get().get();
    }

    public static class IdentityPointcuts {
        static AtomicInteger index = new AtomicInteger(1);
        ReentrantLock lock = new ReentrantLock();
        int identify;
        ConcurrentHashMap<Integer, Integer> pointcutIndexSet = new ConcurrentHashMap<>();
        Cache<ClassLoader, Boolean> cache = CacheBuilder.newBuilder().weakKeys().build();

        public IdentityPointcuts() {
            this.identify = index.incrementAndGet();
        }

        public Boolean checkPointcutExist(Integer pointcutIndex) {
            return this.pointcutIndexSet.putIfAbsent(pointcutIndex, pointcutIndex) != null;
        }

        public int getIdentify() {
            return this.identify;
        }

        public boolean checkClassloaderExist() {
            ClassLoader loader = getCurrentClassLoader();
            if (cache.getIfPresent(loader) == null) {
                cache.put(loader, true);
                return false;
            }
            return true;
        }

        public void lock() {
            this.lock.lock();
        }

        public void unlock() {
            this.lock.unlock();
        }

        /**
         * Some empty slots may appear, the effect can be ignored and can be optimized later
         */
        public void tryRelease() {
            int id = this.identify;
            index.compareAndSet(id, id - 1);
        }
    }
}
