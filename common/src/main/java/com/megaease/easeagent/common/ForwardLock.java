package com.megaease.easeagent.common;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;

import java.util.Set;

public class ForwardLock {
    private static final ThreadLocal<Set<ForwardLock>> MARK = new ThreadLocal<Set<ForwardLock>>(){
        @Override
        protected Set<ForwardLock> initialValue() {
            return Sets.newHashSet();
        }
    };

    public <T> Release<T> acquire(Supplier<T> supplier) {
        if (!MARK.get().add(this)) return new Release<T>() {
            @Override
            public void apply(Consumer<T> c) { }
        };

        final T value = supplier.get();

        return new Release<T>() {
            @Override
            public void apply(Consumer<T> c) {
                MARK.get().remove(ForwardLock.this);
                c.accept(value);
            }
        };
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Release<T> {
        void apply(Consumer<T> c);

    }

    public interface Supplier<T> {
        T get();
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Consumer<T> {
        void accept(T t);
    }
}
