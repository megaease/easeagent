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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.megaease.easeagent.core.plugin.transformer.DynamicFieldAdvice.DynamicInstanceInit;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.util.concurrent.ConcurrentHashMap;

public class DynamicFieldTransformer implements AgentBuilder.Transformer {
    private final static Logger log = LoggerFactory.getLogger(DynamicFieldTransformer.class);
    private final static ConcurrentHashMap<String, Cache<ClassLoader, Boolean>> fieldMap = new ConcurrentHashMap<>();

    private final String fieldName;
    private final Class<?> accessor;
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
            .advice(ElementMatchers.isConstructor(), DynamicInstanceInit.class.getName());
    }

    @Override
    public DynamicType.Builder<?> transform(DynamicType.Builder<?> b,
                                            TypeDescription td, ClassLoader cl, JavaModule m) {
        if (check(td, this.accessor, cl) && this.fieldName != null) {
            try {
                b = b.defineField(this.fieldName, Object.class, Opcodes.ACC_PRIVATE)
                    .implement(this.accessor)
                    .intercept(FieldAccessor.ofField(this.fieldName));
                        //.setsArgumentAt(0).andThen(FixedValue.value(NullObject.NULL)));
            } catch (Exception e) {
                log.debug("Type:{} add extend field again!", td.getName());
            }
            return transformer.transform(b, td, cl, m);
        }
        return b;
    }

    /**
     * Avoiding add a accessor interface to a class repeatedly
     * @param td    represent the class to be enhanced
     * @param accessor access interface class
     * @param cl current classloader
     * @return return true when it is the first time
     */
    private static boolean check(TypeDescription td, Class<?> accessor, ClassLoader cl) {
        String key = td.getCanonicalName() + accessor.getCanonicalName();

        Cache<ClassLoader, Boolean> checkCache = fieldMap.get(key);
        if (checkCache == null) {
            Cache<ClassLoader, Boolean> cache = CacheBuilder.newBuilder().weakKeys().build();
            cache.put(cl, true);
            checkCache = fieldMap.putIfAbsent(key, cache);
            if (checkCache == null) {
                return true;
            }
        }

        return checkCache.getIfPresent(cl) == null;
    }
}
