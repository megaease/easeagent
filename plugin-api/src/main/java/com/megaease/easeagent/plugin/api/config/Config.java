package com.megaease.easeagent.plugin.api.config;

import java.util.List;
import java.util.Set;

public interface Config {
    String domain();

    String namespace();

    String id();

    boolean hasProperty(String property);

    String getString(String property);

    Integer getInt(String property);

    Boolean getBoolean(String property);

    default Boolean getBoolean(String property, Boolean defaultValue) {
        Boolean ret;
        if (!hasProperty(property)) {
            return defaultValue;
        } else {
            ret = getBoolean(property);
            return ret != null ? ret : defaultValue;
        }
    }

    Double getDouble(String property);

    Long getLong(String property);

    List<String> getStringList(String property);

    void addChangeListener(ConfigChangeListener listener);

    Set<String> keySet();
}
