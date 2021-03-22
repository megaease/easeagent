package com.megaease.easeagent.core.utils;

import java.util.function.Supplier;

public class ThreadUtils {
    public static <V> V callWithClassLoader(ClassLoader use, Supplier<V> runnable) {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(use);
        try {
            return runnable.get();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
