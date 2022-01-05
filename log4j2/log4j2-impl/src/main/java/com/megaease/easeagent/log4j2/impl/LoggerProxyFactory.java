/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.log4j2.impl;

import com.megaease.easeagent.log4j2.api.AgentLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.slf4j.Logger;

/**
 * Log4j implementation of SLF4J ILoggerFactory interface.
 */
public class LoggerProxyFactory extends AbstractLoggerAdapter<AgentLoggerProxy> {
    private static final LoggerProxyFactory LOGGER_FACTORY = new LoggerProxyFactory();

    private static final String FQCN = LoggerProxyFactory.class.getName();
    private static final String PACKAGE = "org.slf4j";
    private static final String TO_SLF4J_CONTEXT = "org.apache.logging.slf4j.SLF4JLoggerContext";

    private final String loggerFqcn;

    public LoggerProxyFactory() {
        this(AgentLogger.class.getName());
    }

    public LoggerProxyFactory(String loggerFqcn) {
        this.loggerFqcn = loggerFqcn;
    }

    public static Slf4jLogger getAgentLogger(String name) {
        return new Slf4jLogger(LOGGER_FACTORY.getLogger(name));
    }

    @Override
    protected AgentLoggerProxy newLogger(final String name, final LoggerContext context) {
        final String key = Logger.ROOT_LOGGER_NAME.equals(name) ? LogManager.ROOT_LOGGER_NAME : name;
        return new AgentLoggerProxy(validateContext(context).getLogger(key), name, loggerFqcn);
    }

    @Override
    protected LoggerContext getContext() {
        final Class<?> anchor = StackLocatorUtil.getCallerClass(FQCN, PACKAGE);
        return anchor == null ? LogManager.getContext() : getContext(StackLocatorUtil.getCallerClass(anchor));
    }

    private LoggerContext validateContext(final LoggerContext context) {
        if (TO_SLF4J_CONTEXT.equals(context.getClass().getName())) {
            throw new LoggingException("log4j-slf4j-impl cannot be present with log4j-to-slf4j");
        }
        return context;
    }
}
