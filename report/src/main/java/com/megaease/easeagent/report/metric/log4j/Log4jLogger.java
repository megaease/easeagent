package com.megaease.easeagent.report.metric.log4j;

import org.apache.logging.log4j.MarkerManager;
import org.slf4j.Logger;
import org.slf4j.Marker;

public class Log4jLogger implements Logger {

    private org.apache.logging.log4j.core.Logger logger;

    public Log4jLogger(org.apache.logging.log4j.core.Logger logger) {
        this.logger = logger;
    }


    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        logger.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {

        return logger.isTraceEnabled(new Log4jMarker(marker));
    }

    @Override
    public void trace(Marker marker, String msg) {
        logger.trace(new Log4jMarker(marker), msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        logger.trace(new Log4jMarker(marker), format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(new Log4jMarker(marker), format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        logger.trace(new Log4jMarker(marker), format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(new Log4jMarker(marker), msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        logger.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(new Log4jMarker(marker));
    }

    @Override
    public void debug(Marker marker, String msg) {
        logger.debug(new Log4jMarker(marker), msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        logger.debug(new Log4jMarker(marker), format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(new Log4jMarker(marker), format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(new Log4jMarker(marker), format, arguments);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(new Log4jMarker(marker), msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        logger.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(new Log4jMarker(marker));
    }

    @Override
    public void info(Marker marker, String msg) {
        logger.info(new Log4jMarker(marker), msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        logger.info(new Log4jMarker(marker), format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(new Log4jMarker(marker), format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... arguments) {
        logger.info(new Log4jMarker(marker), format, arguments);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        logger.info(new Log4jMarker(marker), msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object... arguments) {
        logger.warn(format, arguments);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(new Log4jMarker(marker));
    }

    @Override
    public void warn(Marker marker, String msg) {
        logger.warn(new Log4jMarker(marker), msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        logger.warn(new Log4jMarker(marker), format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(new Log4jMarker(marker), format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(new Log4jMarker(marker), format, arguments);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(new Log4jMarker(marker), msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        logger.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(new Log4jMarker(marker));
    }

    @Override
    public void error(Marker marker, String msg) {
        logger.error(new Log4jMarker(marker), msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        logger.error(new Log4jMarker(marker), format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(new Log4jMarker(marker), format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... arguments) {
        logger.error(new Log4jMarker(marker), format, arguments);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        logger.error(new Log4jMarker(marker), msg, t);
    }


    public static class Log4jMarker extends MarkerManager.Log4jMarker {
        private Marker marker;

        public Log4jMarker(String name) {
            super(name);
        }

        public Log4jMarker(Marker marker) {
            super(marker == null ? "" : marker.getName());
        }

    }
}
