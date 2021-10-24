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

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class SpringRedisTracingInterceptor implements AgentInterceptor {

    private static final String CURRENT_SPAN = SpringRedisTracingInterceptor.class.getName() + ".currentSpan";

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Tracer tracer = Tracing.currentTracer();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan != null) {
            context.put(CURRENT_SPAN, currentSpan);
        }
        this.innerBefore(methodInfo, context, chain);
        chain.doBefore(methodInfo, context);
    }

    public void innerBefore(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Span currentSpan = ContextUtils.getFromContext(context, CURRENT_SPAN);
        if (currentSpan == null) {
            return;
        }
        String name = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {

        return chain.doAfter(methodInfo, context);
    }
}
