package com.megaease.easeagent;

import com.google.auto.service.AutoService;

import java.lang.annotation.Retention;
import java.util.HashSet;
import java.util.Set;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * {@link ForwardDetection} would help you to avoid duplicated action in a call chain like filter, when multiple sub types of one interface or abstract class.
 */
public abstract class ForwardDetection {

    private ForwardDetection() {}

    @Retention(RUNTIME)
    @interface Key {}

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    private static class SetThreadLocal extends ThreadLocal<Set<String>> {
        @Override
        protected Set<String> initialValue() {
            return new HashSet<String>();
        }

    }

    @AutoService(AppendBootstrapClassLoaderSearch.class)
    public static class Mark {

        private final static ThreadLocal<Set<String>> CURRENT = new SetThreadLocal();

        public static boolean markIfAbsent(String key) {
            return CURRENT.get().add(key);
        }

        public static boolean clear(String key) {
            return CURRENT.get().remove(key);
        }
    }
}
