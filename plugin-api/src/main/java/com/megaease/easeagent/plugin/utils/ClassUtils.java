package com.megaease.easeagent.plugin.utils;

public class ClassUtils {
    public static boolean hasClass(String className) {
        try {
            Thread.currentThread().getContextClassLoader().loadClass(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static abstract class TypeChecker {
        protected final String className;
        private final boolean hasClass;

        public TypeChecker(String className) {
            this.className = className;
            this.hasClass = ClassUtils.hasClass(className);
        }

        public boolean isHasClass() {
            return hasClass;
        }

        public boolean hasClassAndIsType(Object o) {
            if (!isHasClass()) {
                return false;
            }
            return isType(o);
        }

        protected abstract boolean isType(Object o);

    }
}
