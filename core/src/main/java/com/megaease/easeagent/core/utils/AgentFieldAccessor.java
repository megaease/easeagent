package com.megaease.easeagent.core.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentFieldAccessor {

    private static final Map<String, Field> FIELD_MAP = new ConcurrentHashMap<>();

    public static void setFieldValue(Field field, Object target, Object fieldValue) {
        try {
            field.set(target, fieldValue);
        } catch (IllegalAccessException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Field field, Object target) {
        try {
            return (T) field.get(target);
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    public static Field getFieldFromClass(Class<?> clazz, String fieldName) {
        Field field = FIELD_MAP.get(clazz.getName());
        if (field != null) {
            return field;
        }
        try {
            field = clazz.getField(fieldName);
            field.setAccessible(true);
            FIELD_MAP.put(clazz.getName(), field);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }
}
