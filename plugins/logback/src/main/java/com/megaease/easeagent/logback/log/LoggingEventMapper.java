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
package com.megaease.easeagent.logback.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogData;
import com.megaease.easeagent.plugin.api.otlp.common.AgentLogDataImpl;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

import java.io.PrintWriter;
import java.io.StringWriter;

public class LoggingEventMapper {
    public static final LoggingEventMapper INSTANCE = new LoggingEventMapper();

    public AgentLogData mapLoggingEvent(ILoggingEvent loggingEvent) {
        AgentLogDataImpl.Builder builder = AgentLogDataImpl.builder();
        // logger
        String logger = loggingEvent.getLoggerName();
        if (logger == null || logger.isEmpty()) {
            logger = "ROOT";
        }
        builder.logger(logger);
        // message
        String message = loggingEvent.getFormattedMessage();
        if (message != null) {
            builder.body(message);
        }

        // time
        long timestamp = loggingEvent.getTimeStamp();
        builder.epochMills(timestamp);

        // level
        Level level = loggingEvent.getLevel();
        if (level != null) {
            builder.severity(levelToSeverity(level));
            builder.severityText(level.levelStr);
        }

        AttributesBuilder attrBuilder = builder.getAttributesBuilder();
        // throwable
        Object throwableProxy = loggingEvent.getThrowableProxy();
        Throwable throwable = null;
        if (throwableProxy instanceof ThrowableProxy) {
            // there is only one other subclass of ch.qos.logback.classic.spi.
            // IThrowableProxy and it is only used for logging exceptions over the wire
            throwable = ((ThrowableProxy) throwableProxy).getThrowable();
        }
        if (throwable != null) {
            setThrowable(attrBuilder, throwable);
        }

        Thread currentThread = Thread.currentThread();
        builder.threadName(currentThread.getName());
        attrBuilder.put(SemanticAttributes.THREAD_NAME, currentThread.getName());
        attrBuilder.put(SemanticAttributes.THREAD_ID, currentThread.getId());

        // span context
        builder.spanContext();

        return builder.build();
    }

    private static void setThrowable(AttributesBuilder attrsBuilder, Throwable throwable) {
        attrsBuilder.put(SemanticAttributes.EXCEPTION_TYPE, throwable.getClass().getName());
        attrsBuilder.put(SemanticAttributes.EXCEPTION_MESSAGE, throwable.getMessage());
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        attrsBuilder.put(SemanticAttributes.EXCEPTION_STACKTRACE, writer.toString());
    }

    private static Severity levelToSeverity(Level level) {
        switch (level.levelInt) {
            case Level.ALL_INT:
            case Level.TRACE_INT:
                return Severity.TRACE;
            case Level.DEBUG_INT:
                return Severity.DEBUG;
            case Level.INFO_INT:
                return Severity.INFO;
            case Level.WARN_INT:
                return Severity.WARN;
            case Level.ERROR_INT:
                return Severity.ERROR;
            case Level.OFF_INT:
            default:
                return Severity.UNDEFINED_SEVERITY_NUMBER;
        }
    }
}
