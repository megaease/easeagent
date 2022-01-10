/*
 * Copyright (c) 2017, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.log4j2.api;

import com.megaease.easeagent.log4j2.ClassloaderSupplier;
import com.megaease.easeagent.log4j2.exception.Log4j2Exception;

import javax.annotation.Nonnull;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

public class AgentLoggerFactory<T extends AgentLogger> {
    private final AgentLogger agentLogger;
    private final ClassLoader classLoader;
    private final Object factory;
    private final Method method;
    private final Function<Logger, T> loggerSupplier;
    private final Mdc mdc;

    private AgentLoggerFactory(@Nonnull ClassLoader classLoader,
                               @Nonnull Object factory, @Nonnull Method method,
                               @Nonnull Function<Logger, T> loggerSupplier, @Nonnull Mdc mdc) {
        this.classLoader = classLoader;
        this.factory = factory;
        this.method = method;
        this.loggerSupplier = loggerSupplier;
        this.mdc = mdc;
        this.agentLogger = this.getLogger(AgentLoggerFactory.class.getName());
    }

    public static <T extends AgentLogger> Builder<T> builder(ClassloaderSupplier classLoaderSupplier,
                                                             Function<Logger, T> loggerSupplier,
                                                             Class<T> tClass) {
        ClassLoader classLoader = Objects.requireNonNull(classLoaderSupplier.get(), "classLoader must not be null.");
        return new Builder<>(classLoader, loggerSupplier, tClass);
    }

    public <N extends AgentLogger> AgentLoggerFactory<N> newFactory(Function<Logger, N> loggerSupplier, Class<N> tClass) {
        try {
            return new Builder<N>(classLoader, loggerSupplier, tClass).build();
        } catch (ClassNotFoundException | NoSuchMethodException
            | NoSuchFieldException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
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
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new Log4j2Exception(e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

    public Mdc mdc() {
        return mdc;
    }

    public static class Builder<T extends AgentLogger> {
        private final ClassLoader classLoader;
        private final Function<java.util.logging.Logger, T> loggerSupplier;
        private final Class<T> tClass;

        public Builder(@Nonnull ClassLoader classLoader, @Nonnull Function<Logger, T> loggerSupplier, @Nonnull Class<T> tClass) {
            this.classLoader = classLoader;
            this.loggerSupplier = loggerSupplier;
            this.tClass = tClass;
        }

        public AgentLoggerFactory<T> build() throws ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException {

            ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(classLoader);
                Class<?> clazz = classLoader.loadClass("com.megaease.easeagent.log4j2.impl.LoggerProxyFactory");
                Class<?> parameterTypes = classLoader.loadClass(String.class.getName());
                Constructor<?> constructor = clazz.getDeclaredConstructor(String.class);
                Object factory = constructor.newInstance(tClass.getName());
                Method method = clazz.getDeclaredMethod("getAgentLogger", parameterTypes);
                return new AgentLoggerFactory<>(classLoader, factory, method, loggerSupplier, buildMdc());
            } finally {
                Thread.currentThread().setContextClassLoader(oldClassLoader);
            }
        }


        @SuppressWarnings("unchecked")
        private Mdc buildMdc() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
            Class<?> mdcClazz = classLoader.loadClass("com.megaease.easeagent.log4j2.impl.MdcProxy");
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
