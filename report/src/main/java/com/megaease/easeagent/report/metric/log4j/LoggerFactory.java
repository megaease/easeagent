package com.megaease.easeagent.report.metric.log4j;

import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;

public class LoggerFactory {

    // Independent logger context as an anchor for loggers in the metrics
    private static LoggerContext loggerContext = new LoggerContext("ROOT");

    /**
     * Adapt to the {@link Logger} from {@link org.apache.logging.log4j.core.Logger} as
     * the {@code sdk.metrics.reporter.SpdbcccSlf4jReporter} write metrics via {@link Logger}
     *
     * @param name logger name
     * @return a wrapped {@link Logger}
     */
    public static Logger getLogger(String name) {
        return new Log4jLogger(loggerContext.getLogger(name));
    }

    public static LoggerContext getLoggerContext() {
        return loggerContext;
    }

}
