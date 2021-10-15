package com.megaease.easeagent.log4j2.proxy;

public class Level extends java.util.logging.Level {
    public static final int OFF_VALUE = 1;
    public static final int FATAL_VALUE = 2;
    public static final int ERROR_VALUE = 3;
    public static final int WARN_VALUE = 4;
    public static final int INFO_VALUE = 5;
    public static final int DEBUG_VALUE = 6;
    public static final int TRACE_VALUE = 7;
    public static final int ALL_VALUE = 8;


    public static final Level OFF = new Level("OFF", OFF_VALUE);
    public static final Level FATAL = new Level("FATAL", FATAL_VALUE);
    public static final Level ERROR = new Level("ERROR", ERROR_VALUE);
    public static final Level WARN = new Level("WARN", WARN_VALUE);
    public static final Level INFO = new Level("INFO", INFO_VALUE);
    public static final Level DEBUG = new Level("DEBUG", DEBUG_VALUE);
    public static final Level TRACE = new Level("TRACE", TRACE_VALUE);
    public static final Level ALL = new Level("ALL", ALL_VALUE);


    protected Level(String name, int value) {
        super(name, value);
    }
}
