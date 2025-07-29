package com.megaease.easeagent.plugin.httpurlconnection.jdk17.interceptor;

import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.field.DynamicFieldAccessor;

import java.util.HashSet;
import java.util.Set;

public class DynamicFieldUtils {
    private static final Logger logger = EaseAgent.getLogger(DynamicFieldUtils.class);

    public static Set<String> getOrCreateSet(Object obj) {
        if (!(obj instanceof DynamicFieldAccessor)) {
            logger.warn("java.net.HttpURLConnection must implements " + DynamicFieldAccessor.class.getName());
            return null;
        }
        Object fieldValue = AgentDynamicFieldAccessor.getDynamicFieldValue(obj);
        Set<String> set;
        if (fieldValue instanceof Set) {
            set = AgentDynamicFieldAccessor.getDynamicFieldValue(obj);
        } else {
            set = new HashSet<>();
            AgentDynamicFieldAccessor.setDynamicFieldValue(obj, set);
        }
        return set;
    }

    public static boolean enterKey(Object obj, String key) {
        Set<String> set = getOrCreateSet(obj);
        if (set == null) {
            return false;
        }
        return set.add(key);
    }
}
