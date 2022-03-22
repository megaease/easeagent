/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SystemEnv {
    private static final Map<String, String> ENVIRONMENTS = new ConcurrentHashMap<>();

    public static String get(String name) {
        String value = ENVIRONMENTS.get(name);
        if (value != null) {
            return value;
        }
        String result = System.getenv(name);
        if (result == null) {
            return null;
        }
        synchronized (ENVIRONMENTS) {
            value = ENVIRONMENTS.get(name);
            if (value != null) {
                return value;
            }
            value = result;
            ENVIRONMENTS.put(name, value);
            return value;
        }
    }

    public static void set(String name, String value) {
        ENVIRONMENTS.put(name, value);
    }
}
