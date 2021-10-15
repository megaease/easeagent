package com.megaease.easeagent.core.log;

import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.log4j2.impl.AgentLoggerFactory;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Logger;

public class LoggerFactoryImpl implements ILoggerFactory {
    private final AgentLoggerFactory<LoggerImpl> loggerFacotry;

    public LoggerFactoryImpl(AgentLoggerFactory<LoggerImpl> loggerFacotry) {
        this.loggerFacotry = loggerFacotry;
    }

    @Override
    public Logger getLogger(String name) {
        return loggerFacotry.getLogger(name);
    }

    public AgentLoggerFactory<LoggerImpl> facotry() {
        return loggerFacotry;
    }

    public static LoggerFactoryImpl build() {
        AgentLoggerFactory<LoggerImpl> loggerFacotry = LoggerFactory.newFactory(LoggerImpl.LOGGER_SUPPLIER, LoggerImpl.class);
        if (loggerFacotry == null) {
            return null;
        }

        return new LoggerFactoryImpl(loggerFacotry);
    }
}
