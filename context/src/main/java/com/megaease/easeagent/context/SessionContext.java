/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.context;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpCleaner;
import com.megaease.easeagent.plugin.bridge.NoOpIPluginConfig;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.field.NullObject;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("unused, unchecked")
public class SessionContext implements InitializeContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionContext.class);
    private static final Setter NOOP_SETTER = (name, value) -> {
    };
    private ITracing tracing = NoOpTracer.NO_OP_TRACING;

    private Supplier<InitializeContext> supplier;
    private final Deque<IPluginConfig> configs = new ArrayDeque<>();
    private final Deque<Object> retStack = new ArrayDeque<>();
    private final Deque<RetBound> retBound = new ArrayDeque<>();

    private final Map<Object, Object> context = new HashMap<>();
    private final Map<Object, Integer> entered = new HashMap<>();
    private boolean hasCleaner = false;

    @Override
    public boolean isNoop() {
        return false;
    }

    public Supplier<InitializeContext> getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier<InitializeContext> supplier) {
        this.supplier = supplier;
    }

    @Override
    public Tracing currentTracing() {
        return NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }

    @Override
    public <V> V get(Object key) {
        return change(context.get(key));
    }

    @Override
    public <V> V remove(Object key) {
        return change(context.remove(key));
    }

    @SuppressWarnings("unchecked")
    private <V> V change(Object o) {
        return o == null ? null : (V) o;
    }

    @Override
    public <V> V put(Object key, V value) {
        context.put(key, value);
        return value;
    }

    @Override
    public <V> V putLocal(String key, V value) {
        assert this.retBound.peek() != null;
        this.retBound.peek().put(key, value);
        return value;
    }

    @Override
    public <V> V getLocal(String key) {
        assert this.retBound.peek() != null;
        return change(this.retBound.peek().get(key));
    }

    @Override
    public IPluginConfig getConfig() {
        if (configs.isEmpty()) {
            LOGGER.warn("context.configs was empty.");
            return NoOpIPluginConfig.INSTANCE;
        }
        return configs.peek();
    }

    @Override
    public void pushConfig(IPluginConfig config) {
        configs.push(config);
    }

    @Override
    public IPluginConfig popConfig() {
        if (configs.isEmpty()) {
            LOGGER.warn("context.configs was empty.");
            return NoOpIPluginConfig.INSTANCE;
        }
        return configs.pop();
    }

    @Override
    public int enter(Object key) {
        Integer count = entered.get(key);
        if (count == null) {
            count = 1;
        } else {
            count++;
        }
        entered.put(key, count);
        return count;
    }

    @Override
    public int exit(Object key) {
        Integer count = entered.get(key);
        if (count == null) {
            return 0;
        }
        entered.put(key, count - 1);
        return count;
    }

    @Override
    public AsyncContext exportAsync() {
        return AsyncContextImpl.build(tracing.exportAsync(), supplier, context);
    }

    @Override
    public Cleaner importAsync(AsyncContext snapshot) {
        Scope scope = tracing.importAsync(snapshot.getSpanContext());
        context.putAll(snapshot.getAll());
        if (hasCleaner) {
            return new AsyncCleaner(scope, false);
        } else {
            hasCleaner = true;
            return new AsyncCleaner(scope, true);
        }
    }

    @Override
    public RequestContext clientRequest(Request request) {
        return tracing.clientRequest(request);
    }

    @Override
    public RequestContext serverReceive(Request request) {
        return tracing.serverReceive(request);
    }

    @Override
    public Span consumerSpan(MessagingRequest request) {
        return tracing.consumerSpan(request);
    }

    @Override
    public Span producerSpan(MessagingRequest request) {
        return tracing.producerSpan(request);
    }

    @Override
    public Span nextSpan() {
        return tracing.nextSpan();
    }

    /**
     * called by framework to maintain stack
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    public void popToBound() {
        while (this.retStack.size() > this.retBound.peek().size()) {
            this.retStack.pop();
        }
    }

    /**
     * called by framework to maintain stack
     */
    public void pushRetBound() {
        this.retBound.push(new RetBound(this.retStack.size()));
    }

    /**
     * called by framework to maintain stack
     */
    public void popRetBound() {
        this.retBound.pop();
    }

    @Override
    public <T> void push(T obj) {
        if (obj == null) {
            this.retStack.push(NullObject.NULL);
        } else {
            this.retStack.push(obj);
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public <T> T pop() {
        if (this.retStack.size() <= this.retBound.peek().size()) {
            return null;
        }
        Object o = this.retStack.pop();
        if (o == NullObject.NULL) {
            return null;
        }
        return change(o);
    }

    @Override
    public <T> T peek() {
        if (this.retStack.isEmpty()) {
            return null;
        }
        Object o = this.retStack.pop();
        if (o == NullObject.NULL) {
            return null;
        }
        return change(o);
    }

    @Override
    public Runnable wrap(Runnable task) {
        return new CurrentContextRunnable(exportAsync(), task);
    }

    @Override
    public boolean isWrapped(Runnable task) {
        return task instanceof CurrentContextRunnable;
    }

    @Override
    public boolean isNecessaryKeys(String key) {
        return tracing.propagationKeys().contains(key);
    }

    @Override
    public void consumerInject(Span span, MessagingRequest request) {
        Injector<MessagingRequest> injector = tracing.messagingTracing().consumerInjector();
        injector.inject(span, request);
    }

    @Override
    public void producerInject(Span span, MessagingRequest request) {
        Injector<MessagingRequest> injector = tracing.messagingTracing().producerInjector();
        injector.inject(span, request);
    }

    @Override
    public void injectForwardedHeaders(Setter setter) {
        Set<String> fields = ProgressFields.getForwardedHeaders();
        if (fields.isEmpty()) {
            return;
        }
        for (String field : fields) {
            Object o = context.get(field);
            if ((o instanceof String)) {
                setter.setHeader(field, (String) o);
            }
        }
    }

    @Override
    public Cleaner importForwardedHeaders(Getter getter) {
        return importForwardedHeaders(getter, NOOP_SETTER);
    }

    private Cleaner importForwardedHeaders(Getter getter, Setter setter) {
        Set<String> fields = ProgressFields.getForwardedHeaders();
        if (fields.isEmpty()) {
            return NoOpCleaner.INSTANCE;
        }
        List<String> fieldArr = new ArrayList<>(fields.size());
        for (String field : fields) {
            String o = getter.header(field);
            if (o == null) {
                continue;
            }
            fieldArr.add(field);
            this.context.put(field, o);
            setter.setHeader(field, o);
        }
        if (fieldArr.isEmpty()) {
            return NoOpCleaner.INSTANCE;
        }
        return new FieldCleaner(fieldArr);
    }


    public ITracing getTracing() {
        return tracing;
    }

    @Override
    public void setCurrentTracing(ITracing tracing) {
        this.tracing = NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }

    @Override
    public void clear() {
        if (!this.configs.isEmpty()) {
            this.configs.clear();
        }
        if (!this.retStack.isEmpty()) {
            this.retStack.clear();
        }
        if (!this.retBound.isEmpty()) {
            this.retBound.clear();
        }
        if (!this.context.isEmpty()) {
            this.context.clear();
        }
        if (!this.entered.isEmpty()) {
            this.entered.clear();
        }
        this.hasCleaner = false;
    }

    public static class CurrentContextRunnable implements Runnable {
        private final AsyncContext asyncContext;
        private final Runnable task;

        public CurrentContextRunnable(AsyncContext asyncContext, Runnable task) {
            this.asyncContext = asyncContext;
            this.task = task;
        }

        @Override
        public void run() {
            try (Cleaner cleaner = asyncContext.importToCurrent()) {
                task.run();
            }
        }
    }

    private class FieldCleaner implements Cleaner {
        private final List<String> fields;

        public FieldCleaner(List<String> fields) {
            this.fields = fields;
        }

        @Override
        public void close() {
            for (String field : fields) {
                context.remove(field);
            }
        }
    }

    public class AsyncCleaner implements Cleaner {
        private final Scope scope;
        private final boolean clearContext;

        public AsyncCleaner(Scope scope, boolean clearContext) {
            this.scope = scope;
            this.clearContext = clearContext;
        }

        @Override
        public void close() {
            this.scope.close();
            if (clearContext) {
                SessionContext.this.clear();
            }
        }
    }
}
