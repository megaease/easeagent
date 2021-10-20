package com.megaease.easeagent.core.log;

import com.megaease.easeagent.log4j2.api.AgentLogger;

import java.util.function.Function;
import java.util.logging.Logger;

public class LoggerImpl extends AgentLogger implements com.megaease.easeagent.plugin.api.logging.Logger {
    public static final Function<Logger, LoggerImpl> LOGGER_SUPPLIER = logger -> new LoggerImpl(logger);

    public LoggerImpl(Logger logger) {
        super(logger);
    }
}
