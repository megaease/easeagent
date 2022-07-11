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

package com.megaease.easeagent.core.plugin;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.utils.AgentArray;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.plugin.AppendBootstrapLoader;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.interceptor.AgentInterceptorChain;

@AutoService(AppendBootstrapLoader.class)
public final class Dispatcher {

    private Dispatcher() {
    }

    static AgentArray<AgentInterceptorChain> chains = new AgentArray<>();

    /**
     * for chains only modified during related class loading process,
     * so it doesn't need to consider updating process
     * otherwise, chain should store in context, avoiding changed during enter and exit
     */
    public static void enter(int index, MethodInfo info, InitializeContext ctx) {
        AgentInterceptorChain chain = chains.getUncheck(index);
        int pos = 0;
        ContextUtils.setBeginTime(ctx);
        chain.doBefore(info, pos, ctx);
    }

    public static Object exit(int index, MethodInfo info, InitializeContext ctx) {
        AgentInterceptorChain chain = chains.getUncheck(index);
        int pos = chain.size() - 1;
        ContextUtils.setEndTime(ctx);
        return chain.doAfter(info, pos, ctx);
    }

    public static AgentInterceptorChain register(int index, AgentInterceptorChain chain) {
        return chains.putIfAbsent(index, chain);
    }

    // for interceptor
    public static AgentInterceptorChain getChain(int index) {
        return chains.get(index);
    }

    public static boolean updateChain(int index, AgentInterceptorChain chain) {
        return chains.replace(index, chain) != null;
    }
}
