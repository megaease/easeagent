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

package com.megaease.easeagent.zipkin.redis;

import brave.Tracing;
import com.megaease.easeagent.plugin.api.context.ContextCons;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class CommonLettuceTracingInterceptor extends CommonRedisTracingInterceptor {

    public CommonLettuceTracingInterceptor(Tracing tracing, Config config) {
        super(tracing, config);
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String uri = ContextUtils.getFromContext(context, ContextCons.CACHE_URI);
        String cmd = ContextUtils.getFromContext(context, ContextCons.CACHE_CMD);
//        String name = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        this.startTracing(cmd, uri, cmd, context);
        chain.doBefore(methodInfo, context);
    }

}
