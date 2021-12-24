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

package com.megaease.easeagent.zipkin.impl;

import brave.propagation.TraceContext;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.Scope;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AsyncContextImpl implements AsyncContext {
    private final Tracing tracing;
    private final TraceContext traceContext;
    private final Map<Object, Object> context;
    private final Supplier<InitializeContext> supplier;

    private AsyncContextImpl(Tracing tracing, TraceContext traceContext, Supplier<InitializeContext> supplier, Map<Object, Object> context) {
        this.tracing = tracing;
        this.traceContext = traceContext;
        this.supplier = supplier;
        this.context = context;
    }

    public static AsyncContextImpl build(Tracing tracing, TraceContext traceContext, Supplier<InitializeContext> supplier) {
        return build(tracing, traceContext, supplier, null);
    }

    public static AsyncContextImpl build(Tracing tracing, TraceContext traceContext, Supplier<InitializeContext> supplier, Map<Object, Object> context) {
        Map<Object, Object> contextMap = context == null ? new HashMap<>() : new HashMap<>(context);
        return new AsyncContextImpl(tracing, traceContext, supplier, contextMap);
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Tracing getTracer() {
        return tracing;
    }

    @Override
    public Context getContext() {
        return supplier.get();
    }

    @Override
    public Scope importToCurrent() {
        return supplier.get().importAsync(this);
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    @Override
    public Map<Object, Object> getAll() {
        return context;
    }

    @Override
    public void putAll(Map<Object, Object> context) {
        context.putAll(context);
    }
}
