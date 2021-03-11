package com.megaease.easeagent.core.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentFieldAccessor {

    public static final String FIELD_MAP_NAME = "_com_ease_agent_data_map_";

    private static final Map<String, Field> FIELD_MAP = new ConcurrentHashMap<>();

    public static void setFieldValue(Object target) {
        Field field = getFieldFromClass(target.getClass());
        if (field == null) {
            return;
        }
        try {
            field.set(target, new ConcurrentHashMap<>());
        } catch (IllegalAccessException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getData(Object target, Object key) {
        Field field = FIELD_MAP.get(target.getClass().getName());
        if (field == null) {
            return null;
        }
        try {
            Map<Object, Object> map = (Map<Object, Object>) field.get(target);
            if (map == null) {
                return null;
            }
            return (T) map.get(key);
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    public static Field getField(Object target) {
        return getFieldFromClass(target.getClass());
    }

    public static Field getFieldFromClass(Class<?> clazz) {
        Field field = FIELD_MAP.get(clazz.getName());
        if (field != null) {
            return field;
        }
        try {
            field = clazz.getField(FIELD_MAP_NAME);
            field.setAccessible(true);
            FIELD_MAP.put(clazz.getName(), field);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }
}
