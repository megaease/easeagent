package com.hexdecteam.easeagent;

import com.google.auto.service.AutoService;

import java.util.HashSet;
import java.util.Set;

// TODO javadoc
@AutoService(AppendBootstrapClassLoaderSearch.class)
public abstract class ForwardDetection {
    private final static ThreadLocal<Set<String>> CURRENT = new SetThreadLocal();

    public static boolean markIfAbsent(String key) {
        return CURRENT.get().add(key);
    }

    public static boolean clear(String key) {
        return CURRENT.get().remove(key);
    }

    private ForwardDetection() {}

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    private static class SetThreadLocal extends ThreadLocal<Set<String>> {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }
    }
}
