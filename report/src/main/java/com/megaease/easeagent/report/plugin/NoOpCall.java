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
package com.megaease.easeagent.report.plugin;

import com.megaease.easeagent.plugin.report.Call;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoOpCall<V> implements Call<V> {
    private static final Map<Class<?>, NoOpCall> INSTANCE_MAP = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> NoOpCall<T> getInstance(Class<?> clazz) {
        NoOpCall<T> b = INSTANCE_MAP.get(clazz);
        if (b != null) {
            return (NoOpCall<T>)b;
        }
        b = new NoOpCall<>();
        INSTANCE_MAP.put(clazz, b);
        return b;
    }

    @Override
    public V execute() throws IOException {
        return null;
    }
}
