/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.mongodb;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class MongoDBUtils {

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
