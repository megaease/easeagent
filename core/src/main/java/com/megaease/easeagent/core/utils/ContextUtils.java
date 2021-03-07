package com.megaease.easeagent.core.utils;

import java.util.HashMap;
import java.util.Map;

public class ContextUtils {
    private static final String BEGIN_TIME = ContextUtils.class.getName() + "beginTime";
    private static final String END_TIME = ContextUtils.class.getName() + "endTime";
    private static final String RET_VALUE = ContextUtils.class.getName() + "retValue";

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

    public static void setRetValue(Map<Object, Object> context, Object value) {
        if (value == null) {
            return;
        }
        context.put(RET_VALUE, value);
    }

    public static <T> T getRetValue(Map<Object, Object> context) {
        return getFromContext(context, RET_VALUE);
    }

    public static Map<Object, Object> createContext() {
        HashMap<Object, Object> map = new HashMap<>();
        setBeginTime(map);
        return map;
    }

    /**
     * Get data from context
     *
     * @param context Store data
     * @param key     key is the type of data. Like {@code value.getClass()}
     * @param <T>     The type of data
     * @return data
     */
    @SuppressWarnings("unchecked")
    public static <T> T getFromContext(Map<Object, Object> context, Object key) {
        return (T) context.get(key);
    }

}
