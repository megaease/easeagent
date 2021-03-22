package com.megaease.easeagent.report.metric.log4j;

import org.apache.logging.log4j.core.LoggerContext;

public class LoggerFactory {

    // Independent logger context as an anchor for loggers in the metrics
    private static LoggerContext loggerContext = new LoggerContext("ROOT");

    public static LoggerContext getLoggerContext() {
        return loggerContext;
    }

}
