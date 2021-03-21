package com.megaease.easeagent.core.utils;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;

import java.util.WeakHashMap;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class ThreadContextBind {
    private static final WeakHashMap<Thread, ThreadLocalCurrentContext.Context> cache = new WeakHashMap<>();

    public static void bind(Thread thread, ThreadLocalCurrentContext.Context ctx) {
        cache.put(thread, ctx);
    }

    public static ThreadLocalCurrentContext.Context get(Thread thread) {
        return cache.get(thread);
    }
}
