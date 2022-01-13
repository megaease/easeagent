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

package com.megaease.easeagent.plugin.redis.interceptor.tracing;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;

public abstract class CommonRedisTracingInterceptor implements NonReentrantInterceptor {
    private static final Object ENTER = new Object();
    private static final Object SPAN_KEY = new Object();


    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        Span currentSpan = context.currentTracing().currentSpan();
        if (currentSpan.isNoop()) {
            return;
        }
        doTraceBefore(methodInfo, context);
    }

    @Override
    public Object getEnterKey(MethodInfo methodInfo, Context context) {
        return ENTER;
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        this.finishTracing(methodInfo.getThrowable(), context);
    }

    public abstract void doTraceBefore(MethodInfo methodInfo, Context context);

    protected void startTracing(Context context, String name, String uri, String cmd) {
        Span span = context.nextSpan().name(name).start();
        span.kind(Span.Kind.CLIENT);
        span.remoteServiceName("redis");
        context.put(SPAN_KEY, span);
        if (cmd != null) {
            span.tag("redis.method", cmd);
        }
        span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.REDIS.getRemoteType());
        RedirectProcessor.setTagsIfRedirected(Redirect.REDIS, span);
    }

    protected void finishTracing(Throwable throwable, Context context) {
        try {
            Span span = context.get(SPAN_KEY);
            if (span == null) {
                return;
            }
            if (throwable != null) {
                span.error(throwable);
            }
            span.finish();
            context.remove(SPAN_KEY);
        } catch (Exception ignored) {
        }
    }
}
