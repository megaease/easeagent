package com.megaease.easeagent.core.utils;

import com.megaease.easeagent.core.DynamicFieldAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

public class AgentDynamicFieldAccessor {

    private static final Logger logger = LoggerFactory.getLogger(AgentDynamicFieldAccessor.class);

    public static final String DYNAMIC_FIELD_NAME = "ease_agent_dynamic_$$$_data";

    @SuppressWarnings("unchecked")
    public static <T> T getDynamicFieldValue(Object target) {
        if (!(target instanceof DynamicFieldAccessor)) {
//            throw new IllegalArgumentException(target.getClass().getName() + " must implements DynamicFieldAccessor");
            logger.error(target.getClass().getName() + " must implements DynamicFieldAccessor");
            return null;
        }
        return (T) ((DynamicFieldAccessor) target).getEaseAgent$$DynamicField$$Data();
    }

    public static void setDynamicFieldValue(Object target, Object value) {
        if (!(target instanceof DynamicFieldAccessor)) {
//            throw new IllegalArgumentException(target.getClass().getName() + " must implements DynamicFieldAccessor");
            logger.error(target.getClass().getName() + " must implements DynamicFieldAccessor");
            return;
        }
        ((DynamicFieldAccessor) target).setEaseAgent$$DynamicField$$Data(value);
    }

    public static Field getDynamicFieldFromClass(Class<?> clazz) {
        return AgentFieldAccessor.getFieldFromClass(clazz, DYNAMIC_FIELD_NAME);
    }
}
