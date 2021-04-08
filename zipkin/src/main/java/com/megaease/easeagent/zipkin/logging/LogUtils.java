package com.megaease.easeagent.zipkin.logging;

import com.google.auto.service.AutoService;
import com.megaease.easeagent.core.AppendBootstrapClassLoaderSearch;

@AutoService(AppendBootstrapClassLoaderSearch.class)
public class LogUtils {

    private static final String LOG4J_MDC_CLASS_NAME = "org.apache.logging.log4j.ThreadContext";
    private static final String LOGBACK_MDC_CLASS_NAME = "org.slf4j.MDC";

    private static final String LOG4J_CHECK_CLASS_NAME = "org.apache.logging.log4j.core.Appender";
    private static final String LOGBACK_CHECK_CLASS_NAME = "ch.qos.logback.core.Appender";

    private static Boolean LOG4J_LOADED;
    private static Boolean LOGBACK_LOADED;

    public static Class<?> LOG4J_MDC_CLASS;
    public static Class<?> LOGBACK_MDC_CLASS;

    public static Class<?> checkLog4JMDC(ClassLoader classLoader) {
        if (LOG4J_LOADED != null) {
            return LOG4J_MDC_CLASS;
        }
        if (loadClass(classLoader, LOG4J_CHECK_CLASS_NAME) != null) {
            LOG4J_MDC_CLASS = loadClass(classLoader, LOG4J_MDC_CLASS_NAME);
        }
        LOG4J_LOADED = true;
        return LOG4J_MDC_CLASS;
    }

    public static Class<?> checkLogBackMDC(ClassLoader classLoader) {
        if (LOGBACK_LOADED != null) {
            return LOGBACK_MDC_CLASS;
        }
        if (loadClass(classLoader, LOGBACK_CHECK_CLASS_NAME) != null) {
            LOGBACK_MDC_CLASS = loadClass(classLoader, LOGBACK_MDC_CLASS_NAME);
        }
        LOGBACK_LOADED = true;
        return LOGBACK_MDC_CLASS;
    }

    public static Class<?> loadClass(ClassLoader classLoader, String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

//
//    public static Class<?> loadLog4JMDC(ClassLoader classLoader) {
//        if (LOG4J_LOADED != null) {
//            return LOG4J_MDC_CLASS;
//        }
//        LOG4J_MDC_CLASS = loadClass(classLoader, LOG4J_MDC_CLASS_NAME);
//        LOG4J_LOADED = true;
//        return LOG4J_MDC_CLASS;
//    }
//
//    public static Class<?> loadLogBackMDC(ClassLoader classLoader) {
//        if (LOGBACK_LOADED != null) {
//            return LOGBACK_MDC_CLASS;
//        }
//        LOGBACK_MDC_CLASS = loadClass(classLoader, LOGBACK_MDC_CLASS_NAME);
//        LOGBACK_LOADED = true;
//        return LOGBACK_MDC_CLASS;
//    }
}
