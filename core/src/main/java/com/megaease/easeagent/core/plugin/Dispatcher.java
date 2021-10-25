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
import com.megaease.easeagent.core.utils.AgentArray;
import com.megaease.easeagent.plugin.MethodInfo;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public final class Dispatcher {
    static AgentArray<AgentInterceptorChain> chains = new AgentArray<>();

    public static void register(int index, AgentInterceptorChain chain) {
        chains.set(index, chain);
    }

    public static void enter(int index, MethodInfo info, Object ctx) {
        AgentInterceptorChain chain = chains.getUncheck(index);
        chain.doBefore(info, ctx);
    }

    public static Object exit(int index, MethodInfo info, Object ctx) {
        AgentInterceptorChain chain = chains.getUncheck(index);
        return chain.doAfter(info, ctx);
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Advice {
        Object execute(Object... args);
    }
}
