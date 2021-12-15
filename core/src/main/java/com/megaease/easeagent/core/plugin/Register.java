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

package com.megaease.easeagent.core.plugin;

import com.google.common.base.Supplier;
import com.megaease.easeagent.core.QualifiedBean;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Register {
    private static final Logger log = LoggerFactory.getLogger(Register.class);

    private final Iterable<QualifiedBean> beans;
    private final Set<String> applied;

    Register(Iterable<QualifiedBean> beans) {
        this.beans = beans;
        applied = new HashSet<>();
    }

    void apply(String adviceToClassName, ClassLoader external) {
        if (!applied.add(adviceToClassName)) return;

        try {
            final Class<?> aClass = compound(getClass().getClassLoader(), external).loadClass(adviceToClassName);

        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private ClassLoader compound(ClassLoader parent, ClassLoader external) {
        try {
            parent.getClass().getDeclaredMethod("add", ClassLoader.class).invoke(parent, external);
        } catch (Exception e) {
            log.warn("{}, this may be a bug if it was running in production", e.toString());
        }
        return parent;
    }

    private static class DefaultConstructorNew implements Supplier<Object> {
        private final Class<?> aClass;

        DefaultConstructorNew(Class<?> aClass) {
            this.aClass = aClass;
        }

        @Override
        public Object get() {
            try {
                return aClass.newInstance();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
