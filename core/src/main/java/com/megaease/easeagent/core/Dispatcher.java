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

 package com.megaease.easeagent.core;

import com.google.auto.service.AutoService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public final class Dispatcher {
    private final static ConcurrentMap<String, Advice> MAP = new ConcurrentHashMap<String, Advice>();

    public static void register(String name, Advice advice) {
        MAP.put(name, advice);
    }

    public static Object execute(String name, Object... args) {
        return MAP.get(name).execute(args);
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Advice {
        Object execute(Object... args);
    }

}
