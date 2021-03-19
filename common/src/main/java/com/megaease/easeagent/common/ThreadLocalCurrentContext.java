package com.megaease.easeagent.common;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

public class ThreadLocalCurrentContext {
    public static final ThreadLocalCurrentContext DEFAULT = new ThreadLocalCurrentContext(new ThreadLocal<>());
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

    public <C> Callable<C> wrap(Callable<C> task) {
        final Context invocationContext = get();
        class CurrentTraceContextCallable implements Callable<C> {
            @Override
            public C call() throws Exception {
                try (Scope scope = maybeScope(invocationContext)) {
                    return task.call();
                }
            }
        }
        return new CurrentTraceContextCallable();
    }

    /**
     * Wraps the input so that it executes with the same context as now.
     */
    public Runnable wrap(Runnable task) {
        final Context invocationContext = get();
        class CurrentTraceContextRunnable implements Runnable {
            @Override
            public void run() {
                try (Scope scope = maybeScope(invocationContext)) {
                    task.run();
                }
            }
        }
        return new CurrentTraceContextRunnable();
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

    public interface Scope extends Closeable {
        Scope NOOP = new Scope() {
            @Override
            public void close() {
            }

            @Override
            public String toString() {
                return "NoopScope";
            }
        };

        @Override
        void close();
    }

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
    }

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
