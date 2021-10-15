package com.megaease.easeagent.log4j2.impl;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class AgentLoggerFactory<T extends AgentLogger> {
    private final AgentLogger agentLogger;
    private final URLClassLoader classLoader;
    private final Object factory;
    private final Method method;
    private final Function<Logger, T> loggerSupplier;
    private final Mdc mdc;

    private AgentLoggerFactory(@Nonnull URLClassLoader classLoader,@Nonnull Object factory,@Nonnull Method method,@Nonnull Function<Logger, T> loggerSupplier,@Nonnull Mdc mdc) {
        this.classLoader = classLoader;
        this.factory = factory;
        this.method = method;
        this.loggerSupplier = loggerSupplier;
        this.mdc = mdc;
        this.agentLogger = this.getLogger(AgentLoggerFactory.class.getName());
    }

    public static <T extends AgentLogger> Builder<T> builder(Supplier<URL[]> urls, Function<Logger, T> loggerSupplier, Class<T> tClass) {
        URLClassLoader classLoader = new URLClassLoader(Objects.requireNonNull(urls.get(), "urls must not be null."), null);
        return new Builder<T>(classLoader, loggerSupplier, tClass);
    }

    public <N extends AgentLogger> AgentLoggerFactory<N> newFactory(Function<Logger, N> loggerSupplier, Class<N> tClass) {
        try {
            return new Builder<N>(classLoader, loggerSupplier, tClass).build();
        } catch (ClassNotFoundException e) {
            agentLogger.error("new factory fail: {}", e);
        } catch (NoSuchMethodException e) {
            agentLogger.error("new factory fail: {}", e);
        } catch (IllegalAccessException e) {
            agentLogger.error("new factory fail: {}", e);
        } catch (InvocationTargetException e) {
            agentLogger.error("new factory fail: {}", e);
        } catch (InstantiationException e) {
            agentLogger.error("new factory fail: {}", e);
        } catch (NoSuchFieldException e) {
            agentLogger.error("new factory fail: {}", e);
        }
        return null;
    }

    public T getLogger(String name) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            Object o = method.invoke(factory, name);
            Thread.currentThread().setContextClassLoader(oldClassLoader);
            java.util.logging.Logger logger = (java.util.logging.Logger) o;
            // 还原为之前的 ClassLoader
            return loggerSupplier.apply(logger);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public Mdc mdc() {
        return mdc;
    }

    public static class Builder<T extends AgentLogger> {
        private final URLClassLoader classLoader;
        private final Function<java.util.logging.Logger, T> loggerSupplier;
        private final Class<T> tClass;

        public Builder(URLClassLoader classLoader, Function<Logger, T> loggerSupplier, Class<T> tClass) {
            this.classLoader = Objects.requireNonNull(classLoader, "classLoader must not be null.");
            this.loggerSupplier = Objects.requireNonNull(loggerSupplier, "loggerSupplier must not be null.");
            this.tClass = Objects.requireNonNull(tClass, "tClass must not be null.");
        }

        public AgentLoggerFactory<T> build() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {
            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                Class<?> clazz = classLoader.loadClass("com.megaease.easeagent.log4j2.proxy.LoggerProxyFactory");
                Class<?> parameterTypes = classLoader.loadClass(String.class.getName());
                Constructor constructor = clazz.getDeclaredConstructor(String.class);
                Object factory = constructor.newInstance(tClass.getName());
                Method method = clazz.getDeclaredMethod("getAgentLogger", parameterTypes);
                return new AgentLoggerFactory<>(classLoader, factory, method, loggerSupplier, buildMdc());
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }


        private Mdc buildMdc() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
            Class<?> mdcClazz = classLoader.loadClass("com.megaease.easeagent.log4j2.proxy.MdcProxy");
            Field putField = mdcClazz.getDeclaredField("PUT_INSTANCE");
            BiFunction<String, String, Void> put = (BiFunction<String, String, Void>) putField.get(null);

            Field removeField = mdcClazz.getDeclaredField("REMOVE_INSTANCE");
            Function<String, Void> remove = (Function<String, Void>) removeField.get(null);


            Field getField = mdcClazz.getDeclaredField("GET_INSTANCE");
            Function<String, String> get = (Function<String, String>) getField.get(null);
            return new Mdc(put, remove, get);
        }
    }

}
