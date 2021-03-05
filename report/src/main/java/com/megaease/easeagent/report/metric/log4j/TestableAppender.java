package com.megaease.easeagent.report.metric.log4j;

import org.apache.logging.log4j.core.LogEvent;

import java.util.function.Consumer;

public interface TestableAppender {
    /**
     * Add a LogEvent consumer which was wrapped as a appender to logger, <strong>test only</strong>
     *
     * @param logEventConsumer a Consumer for consumer {@link LogEvent}
     */
    void setTestAppender(Consumer<LogEvent> logEventConsumer);
}
