package com.megaease.easeagent.log4j2.supplier;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.function.Supplier;

public class AllUrlsSupplier implements Supplier<URL[]> {
    public static final String USE_ENV = "EASEAGENT-SLF4J2-USE-CURRENT";
    public static volatile boolean ENABLED = false;

    @Override
    public URL[] get() {
        if (!enabled()) {
            return new URL[0];
        }
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (!(classLoader instanceof URLClassLoader)) {
            return null;
        }
        return ((URLClassLoader) classLoader).getURLs();
    }

    private boolean enabled() {
        if (ENABLED) {
            return true;
        }
        String enabledStr = System.getProperty(USE_ENV);
        if (enabledStr == null) {
            return false;
        }
        try {
            return Boolean.parseBoolean(enabledStr);
        } catch (Exception e) {
            return false;
        }
    }
}
