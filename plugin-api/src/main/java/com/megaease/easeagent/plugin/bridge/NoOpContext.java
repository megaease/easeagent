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

package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Cleaner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.RequestContext;
import com.megaease.easeagent.plugin.api.trace.*;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

public class NoOpContext {
    public static final NoopContext NO_OP_CONTEXT = NoopContext.INSTANCE;
    public static final EmptyAsyncContext NO_OP_ASYNC_CONTEXT = EmptyAsyncContext.INSTANCE;
    public static final NoopRequestContext NO_OP_PROGRESS_CONTEXT = NoopRequestContext.INSTANCE;

    public static class NoopContext implements InitializeContext {
        private static final NoopContext INSTANCE = new NoopContext();
        private static final Iterator<String> EMPTY_KEYS = new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public String next() {
                return null;
            }
        };

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public Tracing currentTracing() {
            return NoOpTracer.NO_OP_TRACING;
        }

        @Override
        public <V> V putLocal(String key, V value) {
            return null;
        }

        @Override
        public <V> V getLocal(String key) {
            return null;
        }

        @Override
        public <V> V get(Object key) {
            return null;
        }

        @Override
        public <V> V remove(Object key) {
            return null;
        }

        @Override
        public <V> V put(Object key, V value) {
            return value;
        }

        @Override
        public IPluginConfig getConfig() {
            return NoOpIPluginConfig.INSTANCE;
        }

        @Override
        public int enter(Object key) {
            return 0;
        }

        @Override
        public int exit(Object key) {
            return 0;
        }

        @Override
        public AsyncContext exportAsync() {
            return EmptyAsyncContext.INSTANCE;
        }

        @Override
        public Cleaner importAsync(AsyncContext snapshot) {
            return NoOpCleaner.INSTANCE;
        }

        @Override
        public RequestContext clientRequest(Request request) {
            return NoopRequestContext.INSTANCE;
        }

        @Override
        public RequestContext serverReceive(Request request) {
            return NoopRequestContext.INSTANCE;
        }

        @Override
        public Span consumerSpan(MessagingRequest request) {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public Span producerSpan(MessagingRequest request) {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public Span nextSpan() {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public <T> T pop() {
            return null;
        }

        @Override
        public <T> T peek() {
            return null;
        }

        @Override
        public <T> void push(T obj) {
        }

        @Override
        public Runnable wrap(Runnable task) {
            return task;
        }

        @Override
        public boolean isWrapped(Runnable task) {
            return true;
        }

        @Override
        public boolean isNecessaryKeys(String key) {
            return false;
        }

        @Override
        public void consumerInject(Span span, MessagingRequest request) {

        }

        @Override
        public void producerInject(Span span, MessagingRequest request) {

        }

        @Override
        public void injectForwardedHeaders(Setter setter) {

        }

        @Override
        public Cleaner importForwardedHeaders(Getter getter) {
            return NoOpCleaner.INSTANCE;
        }

        public void setCurrentTracing(ITracing tracing) {
        }

        @Override
        public void pushConfig(IPluginConfig config) {
        }

        @Override
        public IPluginConfig popConfig() {
            return NoOpIPluginConfig.INSTANCE;
        }

        @Override
        public void pushRetBound() {
        }

        @Override
        public void popRetBound() {
        }

        @Override
        public void popToBound() {
        }

        @Override
        public void clear() {

        }
    }


    public static class EmptyAsyncContext implements AsyncContext {
        private static final EmptyAsyncContext INSTANCE = new EmptyAsyncContext();

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public Tracing getTracer() {
            return NoOpTracer.NO_OP_TRACING;
        }

        @Override
        public Context getContext() {
            return NoopContext.INSTANCE;
        }

        @Override
        public Cleaner importToCurrent() {
            return NoOpCleaner.INSTANCE;
        }

        @Override
        public Map<Object, Object> getAll() {
            return Collections.emptyMap();
        }

        @Override
        public void putAll(Map<Object, Object> context) {

        }
    }

    public static class NoopRequestContext implements RequestContext {
        private static final NoopRequestContext INSTANCE = new NoopRequestContext();

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public Span span() {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public Scope scope() {
            return NoOpTracer.NO_OP_SCOPE;
        }

        @Override
        public void setHeader(String name, String value) {

        }

        @Override
        public Map<String, String> getHeaders() {
            return Collections.emptyMap();
        }

        @Override
        public AsyncContext async() {
            return EmptyAsyncContext.INSTANCE;
        }

        @Override
        public Context getContext() {
            return NoopContext.INSTANCE;
        }

        @Override
        public void finish(Response response) {

        }
    }

}
