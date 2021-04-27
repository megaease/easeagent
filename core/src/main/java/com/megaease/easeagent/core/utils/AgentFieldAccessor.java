/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.core.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentFieldAccessor {

    private static final Map<String, Field> FIELD_MAP = new ConcurrentHashMap<>();

    public static void setFieldValue(Object target, String fieldName, Object fieldValue) {
        Field field = getFieldFromClass(target.getClass(), fieldName);
        try {
            field.set(target, fieldValue);
        } catch (IllegalAccessException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, String fieldName) {
        Field field = getFieldFromClass(target.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            return (T) field.get(target);
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    public static Field getFieldFromClass(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        Field field = FIELD_MAP.get(key);
        if (field != null) {
            return field;
        }
        field = innerGetFieldFromClass(clazz, fieldName);
        if (field != null) {
            FIELD_MAP.put(key, field);
        }
        return field;
    }

    public static Field innerGetFieldFromClass(Class<?> clazz, String fieldName) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException ignored) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass.equals(Object.class)) {
                return null;
            }
            return innerGetFieldFromClass(superclass, fieldName);
        }
    }
}
