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
import com.megaease.easeagent.plugin.Const;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.ProgressFields;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.trace.*;
import com.megaease.easeagent.plugin.bridge.NoOpConfig;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class SessionContext implements InitializeContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionContext.class);
    private ITracing tracing = NoOpTracer.NO_OP_TRACING;
    private int sequence = 0;
    private Deque<Config> configs = new ArrayDeque<>();
    private SpanDeque spans = new SpanDeque();
    private Map<Object, Object> context = new HashMap<>();
    private Map<Object, Integer> entered = new HashMap<>();

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
    public int inc() {
        if (sequence < Const.MAX_PLUGIN_STACK) {
            return sequence++;
        }
        LOGGER.warn("the context sequence >= {}, stack overflow.", Const.MAX_PLUGIN_STACK);
        this.clear();
        return sequence++;
    }

    @Override
    public int dec() {
        sequence--;
        if (sequence <= 0) {
            this.clear();
        }
        return sequence;
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
        String[] fields = ProgressFields.getPenetrationFields();
        if (ProgressFields.isEmpty(fields)) {
            return progressContext;
        }
        for (String field : fields) {
            Object o = context.get(field);
            if (o != null && (o instanceof String)) {
                progressContext.setHeader(field, (String) o);
            }
        }
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
        String[] fields = ProgressFields.getPenetrationFields();
        if (ProgressFields.isEmpty(fields)) {
            return span;
        }
        for (String field : fields) {
            Object o = context.get(field);
            if (o != null && (o instanceof String)) {
                request.setHeader(field, (String) o);
            }
        }
        return span;

    }

    @Override
    public Span nextSpan() {
        return tracing.nextSpan();
    }

    @Override
    public void pushSpan(Span span) {
        spans.push(span, sequence);
    }

    @Override
    public Span peekSpan() {
        return spans.peek(sequence);
    }

    @Override
    public Span popSpan() {
        return spans.pop(sequence);
    }

    @Override
    public Runnable wrap(Runnable task) {
        return new CurrentContextRunnable(exportAsync(), task);
    }

    @Override
    public Map<Object, Object> clear() {
        this.tracing = NoOpTracer.NO_OP_TRACING;
        Map<Object, Object> old = Collections.emptyMap();
        if (!this.context.isEmpty()) {
            old = this.context;
            this.context = new HashMap<>();
        }
        if (!this.entered.isEmpty()) {
            this.entered.clear();
        }
        if (!configs.isEmpty()) {
            configs.clear();
        }
        if (!spans.isEmpty()) {
            spans.clear();
        }
        sequence = 0;
        tracing = NoOpTracer.NO_OP_TRACING;
        return old;
    }

    @Override
    public void setCurrentTracing(ITracing tracing) {
        this.tracing = NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
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
