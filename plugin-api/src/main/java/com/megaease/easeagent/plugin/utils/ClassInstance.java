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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ClassInstance<T> {
    private final Class<?> type;

    public ClassInstance() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
        }
        Type t = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        if (!(t instanceof Class)) {
            throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
        }
        type = (Class<?>) t;
    }

    public boolean isInstance(Object o) {
        return type.isInstance(o);
    }

    @SuppressWarnings("unchecked")
    public T to(Object o) {
        return (T) o;
    }
}
