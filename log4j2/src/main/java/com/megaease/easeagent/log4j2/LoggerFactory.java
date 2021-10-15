package com.megaease.easeagent.log4j2;

import com.megaease.easeagent.log4j2.impl.AgentLogger;
import com.megaease.easeagent.log4j2.impl.AgentLoggerFactory;
import com.megaease.easeagent.log4j2.supplier.AllUrlsSupplier;
import com.megaease.easeagent.log4j2.supplier.JarUrlsSupplier;

import java.lang.reflect.InvocationTargetException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;

public class LoggerFactory {
    public static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(LoggerFactory.class.getName());
    protected static final AgentLoggerFactory<AgentLogger> FACTORY;

    static {
        Supplier<String> supplier = () -> "build agent logger fail.";
        AgentLoggerFactory<AgentLogger> factory = null;
        try {
            factory = AgentLoggerFactory.builder(
                new JarUrlsSupplier(new AllUrlsSupplier()),
                AgentLogger.LOGGER_SUPPLIER,
                AgentLogger.class
            ).build();
        } catch (ClassNotFoundException e) {
            logger.log(Level.WARNING, e, supplier);
        } catch (NoSuchMethodException e) {
            logger.log(Level.WARNING, e, supplier);
        } catch (IllegalAccessException e) {
            logger.log(Level.WARNING, e, supplier);
        } catch (InvocationTargetException e) {
            logger.log(Level.WARNING, e, supplier);
        } catch (InstantiationException e) {
            logger.log(Level.WARNING, e, supplier);
        } catch (NoSuchFieldException e) {
            logger.log(Level.WARNING, e, supplier);
        }
        FACTORY = factory;
    }

    public static <N extends AgentLogger> AgentLoggerFactory<N> newFactory(Function<java.util.logging.Logger, N> loggerSupplier, Class<N> tClass) {
        if (FACTORY == null) {
            return null;
        }
        return FACTORY.newFactory(loggerSupplier, tClass);
    }

    public static Logger getLogger(String name) {
        if (FACTORY == null) {
            return new NoopLogger(name);
        }
        return FACTORY.getLogger(name);
    }


    public static Logger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }


    public static class NoopLogger implements Logger {
        private final String name;

        public NoopLogger(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isTraceEnabled() {
            return false;
        }

        @Override
        public void trace(String msg) {

        }

        @Override
        public void trace(String format, Object arg) {

        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {

        }

        @Override
        public void trace(String format, Object... arguments) {

        }

        @Override
        public void trace(String msg, Throwable t) {

        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void debug(String msg) {

        }

        @Override
        public void debug(String format, Object arg) {

        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {

        }

        @Override
        public void debug(String format, Object... arguments) {

        }

        @Override
        public void debug(String msg, Throwable t) {

        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void info(String msg) {

        }

        @Override
        public void info(String format, Object arg) {

        }

        @Override
        public void info(String format, Object arg1, Object arg2) {

        }

        @Override
        public void info(String format, Object... arguments) {

        }

        @Override
        public void info(String msg, Throwable t) {

        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void warn(String msg) {

        }

        @Override
        public void warn(String format, Object arg) {

        }

        @Override
        public void warn(String format, Object... arguments) {

        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {

        }

        @Override
        public void warn(String msg, Throwable t) {

        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void error(String msg) {

        }

        @Override
        public void error(String format, Object arg) {

        }

        @Override
        public void error(String format, Object arg1, Object arg2) {

        }

        @Override
        public void error(String format, Object... arguments) {

        }

        @Override
        public void error(String msg, Throwable t) {

        }
    }
}
