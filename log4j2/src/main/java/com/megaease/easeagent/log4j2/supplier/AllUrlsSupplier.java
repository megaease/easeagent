package com.megaease.easeagent.log4j2.supplier;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

public class AllUrlsSupplier implements Supplier<URL[]> {
    @Override
    public URL[] get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            return null;
        }
        return ((URLClassLoader) classLoader).getURLs();
    }
}
