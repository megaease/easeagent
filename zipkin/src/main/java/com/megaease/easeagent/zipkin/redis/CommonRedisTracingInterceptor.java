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
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;

import java.util.Map;

public class CommonRedisTracingInterceptor implements AgentInterceptor {
    private static final String SPAN_CONTEXT_KEY = CommonRedisTracingInterceptor.class.getName() + "-Span";

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            Span span = ContextUtils.getFromContext(context, Span.class);
            span.error(methodInfo.getThrowable());
        }
        this.finishTracing(context);
        return chain.doAfter(methodInfo, context);
    }

    protected void startTracing(String name, String uri, String cmd, Map<Object, Object> context) {
        Tracer tracer = Tracing.currentTracer();
        Span currentSpan = tracer.currentSpan();
        if (currentSpan == null) {
            return;
        }
        Span span = Tracing.currentTracer().nextSpan().name(name).start();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("redis");
//        if (uri != null) {
//            // TODO: 2021/3/24 add remote host
//        }
        context.put(SPAN_CONTEXT_KEY, span);
        if (cmd != null) {
            span.tag("redis.method", cmd);
        }
    }

    protected void finishTracing(Map<Object, Object> context) {
        try {
            Span span = ContextUtils.getFromContext(context, SPAN_CONTEXT_KEY);
            if (span == null) {
                return;
            }
            span.finish();
            context.remove(SPAN_CONTEXT_KEY);
        } catch (Exception ignored) {
        }
    }
}
