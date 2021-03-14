package com.megaease.easeagent.core.utils;

import java.lang.reflect.Field;

public class AgentDynamicFieldAccessor {

    private static final String BASE_DYNAMIC_FIELD_NAME = "__com_ease_agent_dynamic_$$$_";

    public static final String DYNAMIC_FIELD_NAME = BASE_DYNAMIC_FIELD_NAME + "map";

    public static <T> T getDynamicFieldValue(Object target) {
        return AgentFieldAccessor.getFieldValue(target, DYNAMIC_FIELD_NAME);
    }

    public static void setDynamicFieldValue(Object target, Object value) {
        AgentFieldAccessor.setFieldValue(target, DYNAMIC_FIELD_NAME, value);
    }

    public static Field getDynamicFieldFromClass(Class<?> clazz) {
        return AgentFieldAccessor.getFieldFromClass(clazz, DYNAMIC_FIELD_NAME);
    }

    public static String createFieldName(String subFieldName) {
        return BASE_DYNAMIC_FIELD_NAME + subFieldName;
    }
}
