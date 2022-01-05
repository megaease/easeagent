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
 *
 */

package com.megaease.easeagent.log4j2.impl;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.slf4j.EventDataConverter;
import org.apache.logging.slf4j.Log4jMarker;
import org.apache.logging.slf4j.Log4jMarkerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.slf4j.impl.StaticMarkerBinder;
import org.slf4j.spi.LocationAwareLogger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * SLF4J logger implementation that uses Log4j.
 */
public class AgentLoggerProxy implements LocationAwareLogger, Serializable {

    private static final long serialVersionUID = 4906175597262951747L;
    private static final Marker EVENT_MARKER = MarkerFactory.getMarker("EVENT");
    private static final EventDataConverter CONVERTER = createConverter();

    private final boolean eventLogger;
    private transient ExtendedLogger logger;
    private final String name;
    private final String loggerFqcn;

    public AgentLoggerProxy(final ExtendedLogger logger, final String name, String loggerFqcn) {
        this.logger = logger;
        this.eventLogger = "EventLogger".equals(name);
        this.name = name;
        this.loggerFqcn = loggerFqcn;
    }

    @Override
    public void trace(final String format) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format);
    }

    @Override
    public void trace(final String format, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, o);
    }

    @Override
    public void trace(final String format, final Object arg1, final Object arg2) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, arg1, arg2);
    }

    @Override
    public void trace(final String format, final Object... args) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, args);
    }

    @Override
    public void trace(final String format, final Throwable t) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, null, format, t);
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isEnabled(Level.TRACE, null, null);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return logger.isEnabled(Level.TRACE, getMarker(marker), null);
    }

    @Override
    public void trace(final Marker marker, final String s) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), s);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), s, o);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object o, final Object o1) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), s, o, o1);
    }

    @Override
    public void trace(final Marker marker, final String s, final Object... objects) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), s, objects);
    }

    @Override
    public void trace(final Marker marker, final String s, final Throwable throwable) {
        logger.logIfEnabled(loggerFqcn, Level.TRACE, getMarker(marker), s, throwable);
    }

    @Override
    public void debug(final String format) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format);
    }

    @Override
    public void debug(final String format, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, o);
    }

    @Override
    public void debug(final String format, final Object arg1, final Object arg2) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, arg1, arg2);
    }

    @Override
    public void debug(final String format, final Object... args) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, args);
    }

    @Override
    public void debug(final String format, final Throwable t) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, null, format, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isEnabled(Level.DEBUG, null, null);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return logger.isEnabled(Level.DEBUG, getMarker(marker), null);
    }

    @Override
    public void debug(final Marker marker, final String s) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), s);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), s, o);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object o, final Object o1) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), s, o, o1);
    }

    @Override
    public void debug(final Marker marker, final String s, final Object... objects) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), s, objects);
    }

    @Override
    public void debug(final Marker marker, final String s, final Throwable throwable) {
        logger.logIfEnabled(loggerFqcn, Level.DEBUG, getMarker(marker), s, throwable);
    }

    @Override
    public void info(final String format) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, null, format);
    }

    @Override
    public void info(final String format, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, o);
    }

    @Override
    public void info(final String format, final Object arg1, final Object arg2) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, arg1, arg2);
    }

    @Override
    public void info(final String format, final Object... args) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, args);
    }

    @Override
    public void info(final String format, final Throwable t) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, null, format, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isEnabled(Level.INFO, null, null);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return logger.isEnabled(Level.INFO, getMarker(marker), null);
    }

    @Override
    public void info(final Marker marker, final String s) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), s);
    }

    @Override
    public void info(final Marker marker, final String s, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), s, o);
    }

    @Override
    public void info(final Marker marker, final String s, final Object o, final Object o1) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), s, o, o1);
    }

    @Override
    public void info(final Marker marker, final String s, final Object... objects) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), s, objects);
    }

    @Override
    public void info(final Marker marker, final String s, final Throwable throwable) {
        logger.logIfEnabled(loggerFqcn, Level.INFO, getMarker(marker), s, throwable);
    }

    @Override
    public void warn(final String format) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, null, format);
    }

    @Override
    public void warn(final String format, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, o);
    }

    @Override
    public void warn(final String format, final Object arg1, final Object arg2) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, arg1, arg2);
    }

    @Override
    public void warn(final String format, final Object... args) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, args);
    }

    @Override
    public void warn(final String format, final Throwable t) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, null, format, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isEnabled(Level.WARN, null, null);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return logger.isEnabled(Level.WARN, getMarker(marker), null);
    }

    @Override
    public void warn(final Marker marker, final String s) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), s);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), s, o);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object o, final Object o1) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), s, o, o1);
    }

    @Override
    public void warn(final Marker marker, final String s, final Object... objects) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), s, objects);
    }

    @Override
    public void warn(final Marker marker, final String s, final Throwable throwable) {
        logger.logIfEnabled(loggerFqcn, Level.WARN, getMarker(marker), s, throwable);
    }

    @Override
    public void error(final String format) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format);
    }

    @Override
    public void error(final String format, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, o);
    }

    @Override
    public void error(final String format, final Object arg1, final Object arg2) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, arg1, arg2);
    }

    @Override
    public void error(final String format, final Object... args) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, args);
    }

    @Override
    public void error(final String format, final Throwable t) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, null, format, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isEnabled(Level.ERROR, null, null);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return logger.isEnabled(Level.ERROR, getMarker(marker), null);
    }

    @Override
    public void error(final Marker marker, final String s) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), s);
    }

    @Override
    public void error(final Marker marker, final String s, final Object o) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), s, o);
    }

    @Override
    public void error(final Marker marker, final String s, final Object o, final Object o1) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), s, o, o1);
    }

    @Override
    public void error(final Marker marker, final String s, final Object... objects) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), s, objects);
    }

    @Override
    public void error(final Marker marker, final String s, final Throwable throwable) {
        logger.logIfEnabled(loggerFqcn, Level.ERROR, getMarker(marker), s, throwable);
    }

    @Override
    public void log(final Marker marker, final String fqcn, final int level, final String message, final Object[] params, Throwable throwable) {
        final Level log4jLevel = getLevel(level);
        final org.apache.logging.log4j.Marker log4jMarker = getMarker(marker);

        if (!logger.isEnabled(log4jLevel, log4jMarker, message, params)) {
            return;
        }
        final Message msg;
        if (CONVERTER != null && eventLogger && marker != null && marker.contains(EVENT_MARKER)) {
            msg = CONVERTER.convertEvent(message, params, throwable);
        } else if (params == null) {
            msg = new SimpleMessage(message);
        } else {
            msg = new ParameterizedMessage(message, params, throwable);
            if (throwable != null) {
                throwable = msg.getThrowable();
            }
        }
        logger.logMessage(fqcn, log4jLevel, log4jMarker, msg, throwable);
    }

    private static org.apache.logging.log4j.Marker getMarker(final Marker marker) {
        if (marker == null) {
            return null;
        } else if (marker instanceof Log4jMarker) {
            return ((Log4jMarker) marker).getLog4jMarker();
        } else {
            final Log4jMarkerFactory factory = (Log4jMarkerFactory) StaticMarkerBinder.SINGLETON.getMarkerFactory();
            return ((Log4jMarker) factory.getMarker(marker)).getLog4jMarker();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Always treat de-serialization as a full-blown constructor, by validating the final state of
     * the de-serialized object.
     */
    private void readObject(final ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        // always perform the default de-serialization first
        aInputStream.defaultReadObject();
        logger = LogManager.getContext().getLogger(name);
    }

    /**
     * This is the default implementation of writeObject. Customise if necessary.
     */
    private void writeObject(final ObjectOutputStream aOutputStream) throws IOException {
        // perform the default serialization for all non-transient, non-static fields
        aOutputStream.defaultWriteObject();
    }

    private static EventDataConverter createConverter() {
        try {
            LoaderUtil.loadClass("org.slf4j.ext.EventData");
            return new EventDataConverter();
        } catch (final ClassNotFoundException cnfe) {
            return null;
        }
    }

    private static Level getLevel(final int i) {
        switch (i) {
            case TRACE_INT:
                return Level.TRACE;
            case DEBUG_INT:
                return Level.DEBUG;
            case INFO_INT:
                return Level.INFO;
            case WARN_INT:
                return Level.WARN;
            case ERROR_INT:
                return Level.ERROR;
            default:
                return Level.ERROR;
        }
    }
}
