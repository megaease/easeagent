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

package com.megaease.easeagent.context;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.trace.SpanContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

public class AsyncContextImpl implements AsyncContext {
    private final SpanContext spanContext;
    private final Map<Object, Object> context;
    private final Supplier<InitializeContext> supplier;

    private AsyncContextImpl(SpanContext spanContext, Map<Object, Object> context, Supplier<InitializeContext> supplier) {
        this.spanContext = Objects.requireNonNull(spanContext, "spanContext must not be null");
        this.context = Objects.requireNonNull(context, "context must not be null");
        this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
    }

    public static AsyncContextImpl build(SpanContext spanContext,
                                         Supplier<InitializeContext> supplier,
                                         Map<Object, Object> context) {
        Map<Object, Object> contextMap = context == null ? new HashMap<>() : new HashMap<>(context);
        return new AsyncContextImpl(spanContext, contextMap, supplier);
    }

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public SpanContext getSpanContext() {
        return spanContext;
    }

    @Override
    public Cleaner importToCurrent() {
        return supplier.get().importAsync(this);
    }

    @Override
    public Map<Object, Object> getAll() {
        return context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object o) {
        return (T) this.context.get(o);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V put(Object key, V value) {
        return (V) this.context.put(key, value);
    }
}
