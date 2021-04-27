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

package com.megaease.easeagent.core.utils;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class ThreadLocalCurrentContext {
    public static final ThreadLocalCurrentContext DEFAULT = new ThreadLocalCurrentContext(new InheritableThreadLocal<>());
    final ThreadLocal<Context> local;
    final RevertToNullScope revertToNull;

    public ThreadLocalCurrentContext(ThreadLocal<Context> local) {
        if (local == null) throw new NullPointerException("local == null");
        this.local = local;
        this.revertToNull = new RevertToNullScope(local);
    }

    public Context get() {
        return local.get();
    }

    public Scope newScope(@Nullable Context current) {
        final Context previous = local.get();
        local.set(current);
        Scope result = previous != null ? new RevertToPreviousScope(local, previous) : revertToNull;
        return result;
    }

    public Scope maybeScope(@Nullable Context context) {
        Context current = get();
        if (Objects.equals(current, context)) return Scope.NOOP;
        return newScope(context);
    }

    public void fill(BiConsumer<String, String> consumer, String[] names) {
        final Context ctx = get();
        if (ctx != null) {
            for (String one : names) {
                consumer.accept(one, ctx.get(one));
            }
        }
    }

    /**
     * Wraps the input so that it executes with the same context as now.
     */
    public Runnable wrap(Runnable task) {
        final Context invocationContext = get();
        return new CurrentContextRunnable(this, invocationContext, task);
    }

    public static boolean isWrapped(Runnable task) {
        return task instanceof CurrentContextRunnable;
    }

    public static Context createContext(String... kvs) {
        if (kvs.length % 2 != 0) {
            throw new IllegalArgumentException("size of kvs should be even number");
        }
        final Context ctx = new Context();
        for (int i = 0; i < kvs.length; i += 2) {
            ctx.put(kvs[i], kvs[i + 1]);
        }
        return ctx;
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class CurrentContextRunnable implements Runnable {
        private final ThreadLocalCurrentContext threadLocalCurrentContext;
        private final Context ctx;
        private final Runnable original;

        public CurrentContextRunnable(ThreadLocalCurrentContext threadLocalCurrentContext, Context ctx, Runnable original) {
            this.threadLocalCurrentContext = threadLocalCurrentContext;
            this.ctx = ctx;
            this.original = original;
        }

        @Override
        public void run() {
            try (Scope scope = this.threadLocalCurrentContext.maybeScope(ctx)) {
                original.run();
            }
        }
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Scope extends Closeable {
        Scope NOOP = new NOOPScope();

        @Override
        void close();
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class NOOPScope implements Scope {
        @Override
        public void close() {
        }

        @Override
        public String toString() {
            return "NoopScope";
        }
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Context {
        private final Map<String, String> data = new HashMap<>();

        public String put(String key, String value) {
            return data.put(key, value);
        }

        public String get(String key) {
            return data.get(key);
        }

        public boolean containsKey(String key) {
            return data.containsKey(key);
        }

        @Override
        public String toString() {
            return data.toString();
        }
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    static final class RevertToNullScope implements Scope {
        final ThreadLocal<Context> local;

        RevertToNullScope(ThreadLocal<Context> local) {
            this.local = local;
        }

        @Override
        public void close() {
            local.set(null);
        }
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    static final class RevertToPreviousScope implements Scope {
        final ThreadLocal<Context> local;
        final Context previous;

        RevertToPreviousScope(ThreadLocal<Context> local, Context previous) {
            this.local = local;
            this.previous = previous;
        }

        @Override
        public void close() {
            local.set(previous);
        }
    }
}
