package com.megaease.easeagent.core.log;

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

    public AgentLoggerFactory<LoggerImpl> facotry() {
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
