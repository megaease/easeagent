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

package com.megaease.easeagent.context.log;

import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.log4j2.api.AgentLoggerFactory;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Logger;

import javax.annotation.Nonnull;

public class LoggerFactoryImpl implements ILoggerFactory {
    private final AgentLoggerFactory<LoggerImpl> loggerFactory;

    private LoggerFactoryImpl(@Nonnull AgentLoggerFactory<LoggerImpl> loggerFactory) {
        this.loggerFactory = loggerFactory;
    }

    @Override
    public Logger getLogger(String name) {
        return loggerFactory.getLogger(name);
    }

    public AgentLoggerFactory<LoggerImpl> factory() {
        return loggerFactory;
    }

    public static LoggerFactoryImpl build() {
        AgentLoggerFactory<LoggerImpl> loggerFactory = LoggerFactory.newFactory(LoggerImpl.LOGGER_SUPPLIER, LoggerImpl.class);
        if (loggerFactory == null) {
            return null;
        }

        return new LoggerFactoryImpl(loggerFactory);
    }
}
