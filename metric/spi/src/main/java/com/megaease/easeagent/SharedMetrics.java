package com.megaease.easeagent;


import java.util.Iterator;
import java.util.ServiceLoader;

public abstract class SharedMetrics {

    private static final Metrics SINGLETON;

    static {
        final ClassLoader loader = SharedMetrics.class.getClassLoader();
        final Iterator<Metrics> iterator = ServiceLoader.load(Metrics.class, loader).iterator();
        if (!iterator.hasNext()) throw new IllegalStateException("No metrics found");
        SINGLETON = iterator.next();
    }

    public static Metrics singleton() {
        return SINGLETON;
    }

    private SharedMetrics() { }
}
