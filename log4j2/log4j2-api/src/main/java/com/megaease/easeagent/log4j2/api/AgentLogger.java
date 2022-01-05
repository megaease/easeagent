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

import java.util.function.Function;
import java.util.logging.Logger;

public class AgentLogger implements com.megaease.easeagent.log4j2.Logger {
    public static final Function<Logger, AgentLogger> LOGGER_SUPPLIER = AgentLogger::new;

    private final Logger logger;

    public AgentLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(ILevel.TRACE);
    }

    @Override
    public void trace(String msg) {
        logger.log(ILevel.TRACE, msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.log(ILevel.TRACE, format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.log(ILevel.TRACE, format, new Object[]{arg1, arg2});
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.log(ILevel.TRACE, format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.log(ILevel.TRACE, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(ILevel.DEBUG);
    }

    @Override
    public void debug(String msg) {
        logger.log(ILevel.DEBUG, msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.log(ILevel.DEBUG, format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.log(ILevel.DEBUG, format, new Object[]{arg1, arg2});
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.log(ILevel.DEBUG, format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.log(ILevel.DEBUG, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(ILevel.INFO);
    }

    @Override
    public void info(String msg) {
        logger.log(ILevel.INFO, msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.log(ILevel.INFO, format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.log(ILevel.INFO, format, new Object[]{arg1, arg2});
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.log(ILevel.INFO, format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.log(ILevel.INFO, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(ILevel.WARN);
    }

    @Override
    public void warn(String msg) {
        logger.log(ILevel.WARN, msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.log(ILevel.WARN, format, arg);
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.log(ILevel.WARN, format, new Object[]{arg1, arg2});
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.log(ILevel.WARN, format, arguments);
    }


    @Override
    public void warn(String msg, Throwable t) {
        logger.log(ILevel.WARN, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(ILevel.ERROR);
    }

    @Override
    public void error(String msg) {
        logger.log(ILevel.ERROR, msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.log(ILevel.ERROR, format, arg);

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.log(ILevel.ERROR, format, new Object[]{arg1, arg2});
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.log(ILevel.ERROR, format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.log(ILevel.ERROR, msg, t);
    }
}
