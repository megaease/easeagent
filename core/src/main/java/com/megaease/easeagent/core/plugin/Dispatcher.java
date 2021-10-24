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

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;
import com.megaease.easeagent.core.plugin.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.plugin.interceptor.SupplierChain;
import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public final class Dispatcher {
    private final static ConcurrentMap<String, com.megaease.easeagent.core.Dispatcher.Advice> MAP = new ConcurrentHashMap<String, com.megaease.easeagent.core.Dispatcher.Advice>();

    static ArrayList<AgentInterceptorChain> chains = new ArrayList<>();

    public static void register(int index, AgentInterceptorChain chain) {
        if (chains.size() < index + 1) {
            synchronized (Dispatcher.class) {
                chains.ensureCapacity(index + 1);
            }
        }
        chains.set(index, chain);
    }

    public static void enter(int index, MethodInfo info, Object ctx) {
        AgentInterceptorChain chain = chains.get(index);
        chain.doBefore(info, ctx);
    }

    public static Object exit(int index, MethodInfo info, Object ctx) {
        AgentInterceptorChain chain = chains.get(index);
        return chain.doAfter(info, ctx);
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Advice {
        Object execute(Object... args);
    }
}
