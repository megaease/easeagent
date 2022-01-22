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

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.mongodb.MongoClientSettings;
import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class MongoUtils {

    public static final String CONFIG = MongoUtils.class.getName() + ".Config";
    public static final String METRIC = MongoUtils.class.getName() + ".Metric";
    public static final String EVENT_KEY = MongoUtils.class.getName() + "-Event";


    public static MongoClientSettings mongoClientSettings(MethodInfo methodInfo) {
        if (methodInfo.getArgs() != null) {
            for (Object arg : methodInfo.getArgs()) {
                if (arg instanceof MongoClientSettings) {
                    return (MongoClientSettings) arg;
                }
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
