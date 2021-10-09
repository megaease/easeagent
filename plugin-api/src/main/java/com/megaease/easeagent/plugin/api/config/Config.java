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

    Double getDouble(String property);

    Long getLong(String property);

    List<String> getStringList(String property);

    Runnable addChangeListener(ConfigChangeListener listener);

    Set<String> keySet();
}
