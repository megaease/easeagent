package com.megaease.easeagent.plugin.mongodb;

import lombok.SneakyThrows;

import java.lang.reflect.Field;

public class MongoDBUtils {

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public static <T> T getFieldValue(Object obj, String fieldName) {
        Class<?> clazz = obj.getClass();
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(obj);
    }
}
