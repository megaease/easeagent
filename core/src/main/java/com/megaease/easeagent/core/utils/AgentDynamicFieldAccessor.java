package com.megaease.easeagent.core.utils;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AgentDynamicFieldAccessor {

    public static final String DYNAMIC_FIELD_NAME = "__com_ease_agent_dynamic_$$data_map$$_";

    private static final Map<Object, Object> sharedMap = new ConcurrentHashMap<>();

    public static void initDynamicFieldValue(Object target) {
        Field field = AgentFieldAccessor.getFieldFromClass(target.getClass(), DYNAMIC_FIELD_NAME);
        if (field == null) {
            return;
        }
        AgentFieldAccessor.setFieldValue(field, target, sharedMap);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDynamicFieldValue(Object target, Object key) {
        Field field = AgentFieldAccessor.getFieldFromClass(target.getClass(), DYNAMIC_FIELD_NAME);
        if (field == null) {
            return null;
        }
        Map<Object, Object> map = AgentFieldAccessor.getFieldValue(field, target);
        if (map == null) {
            AgentFieldAccessor.setFieldValue(field, target, sharedMap);
            map = sharedMap;
        }
        return (T) map.get(key);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getSharedValue(Object key) {
        return (T) sharedMap.get(key);
    }

    public static void addSharedValue(Object key, Object value) {
        sharedMap.put(key, value);
    }

    public static Field getDynamicFieldFromClass(Class<?> clazz) {
        return AgentFieldAccessor.getFieldFromClass(clazz, DYNAMIC_FIELD_NAME);
    }
}
