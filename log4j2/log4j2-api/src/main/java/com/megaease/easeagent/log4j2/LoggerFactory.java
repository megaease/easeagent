/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.log4j2;

import com.megaease.easeagent.log4j2.api.AgentLogger;
import com.megaease.easeagent.log4j2.api.AgentLoggerFactory;

import java.util.function.Function;
import java.util.logging.Level;

public class LoggerFactory {
    public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LoggerFactory.class.getName());
    protected static final AgentLoggerFactory<AgentLogger> FACTORY;

    static {
        AgentLoggerFactory<AgentLogger> factory = null;
        try {
            factory = AgentLoggerFactory.builder(
                classLoaderSupplier(),
                AgentLogger.LOGGER_SUPPLIER,
                AgentLogger.class
            ).build();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, String.format("build agent logger factory fail: %s<%s>.", e.getClass().getName(), e.getMessage()));
        }
        FACTORY = factory;
    }

    private static ClassloaderSupplier classLoaderSupplier() {
        return new ClassloaderSupplier.ClassloaderSupplierImpl();
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
            //ignore
        }

        @Override
        public void trace(String format, Object arg) {
            //ignore
        }

        @Override
        public void trace(String format, Object arg1, Object arg2) {
            //ignore
        }

        @Override
        public void trace(String format, Object... arguments) {
            //ignore
        }

        @Override
        public void trace(String msg, Throwable t) {
            //ignore
        }

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void debug(String msg) {
            //ignore
        }

        @Override
        public void debug(String format, Object arg) {
            //ignore
        }

        @Override
        public void debug(String format, Object arg1, Object arg2) {
            //ignore
        }

        @Override
        public void debug(String format, Object... arguments) {
            //ignore
        }

        @Override
        public void debug(String msg, Throwable t) {
            //ignore
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void info(String msg) {
            //ignore
        }

        @Override
        public void info(String format, Object arg) {
            //ignore
        }

        @Override
        public void info(String format, Object arg1, Object arg2) {
            //ignore
        }

        @Override
        public void info(String format, Object... arguments) {
            //ignore
        }

        @Override
        public void info(String msg, Throwable t) {
            //ignore
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void warn(String msg) {
            //ignore
        }

        @Override
        public void warn(String format, Object arg) {
            //ignore
        }

        @Override
        public void warn(String format, Object... arguments) {
            //ignore
        }

        @Override
        public void warn(String format, Object arg1, Object arg2) {
            //ignore
        }

        @Override
        public void warn(String msg, Throwable t) {
            //ignore
        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void error(String msg) {
            //ignore
        }

        @Override
        public void error(String format, Object arg) {
            //ignore
        }

        @Override
        public void error(String format, Object arg1, Object arg2) {
            //ignore
        }

        @Override
        public void error(String format, Object... arguments) {
            //ignore
        }

        @Override
        public void error(String msg, Throwable t) {
            //ignore
        }
    }
}
