package com.megaease.easeagent.core.utils;

import java.util.HashMap;
import java.util.Map;

public class ContextUtils {
    private static final String BEGIN_TIME = "beginTime";
    private static final String END_TIME = "endTime";

    private static void setBeginTime(Map<Object, Object> context) {
        context.put(BEGIN_TIME, System.currentTimeMillis());
    }

    public static Long getBeginTime(Map<Object, Object> context) {
        return (Long) context.get(BEGIN_TIME);
    }

    public static void setEndTime(Map<Object, Object> context) {
        context.put(END_TIME, System.currentTimeMillis());
    }

    public static Long getEndTime(Map<Object, Object> context) {
        return (Long) context.get(END_TIME);
    }

    public static long getDuration(Map<Object, Object> context) {
        return getEndTime(context) - getBeginTime(context);
    }

    public static Map<Object, Object> createContext() {
        HashMap<Object, Object> map = new HashMap<>();
        setBeginTime(map);
        return map;
    }
}
