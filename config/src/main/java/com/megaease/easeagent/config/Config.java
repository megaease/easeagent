package com.megaease.easeagent.config;

import java.util.List;

public interface Config {

    public boolean hasPath(String path);

    public String getString(String name);

    public Integer getInt(String name);

    public Boolean getBoolean(String name);

    public Double getDouble(String name);

    public Long getLong(String name);

    public List<String> getStringList(String name);

    public Runnable addChangeListener(ConfigChangeListener listener);
}
