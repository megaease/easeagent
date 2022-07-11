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

package com.megaease.easeagent.plugin.utils;

public class ClassUtils {
    public static boolean hasClass(String className) {
        try {
            Thread.currentThread().getContextClassLoader().loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static abstract class TypeChecker {
        protected final String className;
        private final boolean hasClass;

        public TypeChecker(String className) {
            this.className = className;
            this.hasClass = ClassUtils.hasClass(className);
        }

        public boolean isHasClass() {
            return hasClass;
        }

        public boolean hasClassAndIsType(Object o) {
            if (!isHasClass()) {
                return false;
            }
            return isType(o);
        }

        protected abstract boolean isType(Object o);

    }
}
