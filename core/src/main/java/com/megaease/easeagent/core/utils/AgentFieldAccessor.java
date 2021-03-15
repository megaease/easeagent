package com.megaease.easeagent.core.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentFieldAccessor {

    private static final Map<String, Field> FIELD_MAP = new ConcurrentHashMap<>();

//    public static void setFieldValue(Object target, Field field, Object fieldValue) {
//        try {
//            field.set(target, fieldValue);
//        } catch (IllegalAccessException ignored) {
//        }
//    }

    public static void setFieldValue(Object target, String fieldName, Object fieldValue) {
        Field field = getFieldFromClass(target.getClass(), fieldName);
        try {
            field.set(target, fieldValue);
        } catch (IllegalAccessException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object target, String fieldName) {
        Field field = getFieldFromClass(target.getClass(), fieldName);
        try {
            return (T) field.get(target);
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

    public static Field getFieldFromClass(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "." + fieldName;
        Field field = FIELD_MAP.get(key);
        if (field != null) {
            return field;
        }
        try {
            field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            FIELD_MAP.put(key, field);
        } catch (NoSuchFieldException ignored) {
        }
        return field;
    }
}
