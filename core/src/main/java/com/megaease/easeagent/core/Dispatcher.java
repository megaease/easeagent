package com.megaease.easeagent.core;

import com.google.auto.service.AutoService;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public final class Dispatcher {
    private final static ConcurrentMap<String, Advice> MAP = new ConcurrentHashMap<String, Advice>();

    public static void register(String name, Advice advice) {
        MAP.put(name, advice);
    }

    public static Object execute(String name, Object... args) {
        return MAP.get(name).execute(args);
    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public interface Advice {
        Object execute(Object... args);
    }

}
