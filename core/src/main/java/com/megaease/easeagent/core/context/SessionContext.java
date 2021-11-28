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

package com.megaease.easeagent.core.context;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpConfig;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.field.NullObject;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.*;

@SuppressWarnings("unused, unchecked")
public class SessionContext implements InitializeContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionContext.class);
    private ITracing tracing = NoOpTracer.NO_OP_TRACING;

    private final Deque<Config> configs = new ArrayDeque<>();
    private final Deque<Object> retStack = new ArrayDeque<>();
    private final Deque<RetBound> retBound = new ArrayDeque<>();

    private final Map<Object, Object> context = new HashMap<>();
    private final Map<Object, Integer> entered = new HashMap<>();

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Tracing currentTracing() {
        return NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }

    @Override
    public <V> V get(Object key) {
        Object v = context.get(key);
        return v == null ? null : (V) v;
    }

    @Override
    public <V> V remove(Object key) {
        Object v = context.remove(key);
        return v == null ? null : (V) v;
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
        return (V) this.retBound.peek().get(key);
    }

    @Override
    public Config getConfig() {
        if (configs.isEmpty()) {
            LOGGER.warn("context.configs was empty.");
            return NoOpConfig.INSTANCE;
        }
        return configs.peek();
    }

    @Override
    public void pushConfig(Config config) {
        configs.push(config);
    }

    @Override
    public Config popConfig() {
        if (configs.isEmpty()) {
            LOGGER.warn("context.configs was empty.");
            return NoOpConfig.INSTANCE;
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
        AsyncContext asyncContext = tracing.exportAsync();
        asyncContext.putAll(context);
        return asyncContext;
    }

    @Override
    public Scope importAsync(AsyncContext snapshot) {
        Scope scope = tracing.importAsync(snapshot);
        context.putAll(snapshot.getAll());
        return new AsyncScope(this, scope);
    }

    @Override
    public ProgressContext nextProgress(Request request) {
        ProgressContext progressContext = tracing.nextProgress(request);
        injectPenetrationFields(progressContext);
        return progressContext;
    }

    @Override
    public ProgressContext importProgress(Request request) {
        ProgressContext progressContext = tracing.importProgress(request);
        String[] fields = ProgressFields.getPenetrationFields();
        if (ProgressFields.isEmpty(fields)) {
            return progressContext;
        }
        for (String field : fields) {
            String value = request.header(field);
            progressContext.setHeader(field, value);
            context.put(field, value);
        }
        return progressContext;
    }

    @Override
    public Span consumerSpan(MessagingRequest request) {
        Span span = tracing.consumerSpan(request);
        String[] fields = ProgressFields.getResponseHoldTagFields();
        if (!ProgressFields.isEmpty(fields)) {
            for (String field : fields) {
                span.tag(field, request.header(field));
            }
        }
        return span;
    }

    @Override
    public Span producerSpan(MessagingRequest request) {
        Span span = tracing.producerSpan(request);
        injectPenetrationFields(request);
        return span;
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
        return (T) o;
    }

    @Override
    public <T> T peek() {
        if (this.retStack.size() <= 0) {
            return null;
        }
        Object o = this.retStack.pop();
        if (o == NullObject.NULL) {
            return null;
        }
        return (T) o;
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
        return tracing.propagationKeys().contains(key) || ProgressFields.getPenetrationFieldsSet().contains(key);
    }

    @Override
    public void consumerInject(Span span, MessagingRequest request) {
        Injector injector = tracing.messagingTracing().consumerInjector();
        injector.inject(span, request);
        injectPenetrationFields(request);
    }

    @Override
    public void producerInject(Span span, MessagingRequest request) {
        Injector injector = tracing.messagingTracing().producerInjector();
        injector.inject(span, request);
        injectPenetrationFields(request);
    }

    @Override
    public void injectPenetrationFields(Setter setter) {
        String[] fields = ProgressFields.getPenetrationFields();
        if (ProgressFields.isEmpty(fields)) {
            return;
        }
        for (String field : fields) {
            Object o = context.get(field);
            if ((o instanceof String)) {
                setter.setHeader(field, (String) o);
            }
        }
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
            try (Scope scope = asyncContext.importToCurr()) {
                task.run();
            }
        }
    }
}
