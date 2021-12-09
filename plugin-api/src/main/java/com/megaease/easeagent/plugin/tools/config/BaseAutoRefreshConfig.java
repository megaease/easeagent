package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import java.util.List;
import java.util.Set;

public class BaseAutoRefreshConfig implements Config, AutoRefreshConfig {
    protected volatile Config config;

    @Override
    public String domain() {
        return config.domain();
    }

    @Override
    public String namespace() {
        return config.namespace();
    }

    @Override
    public String id() {
        return config.id();
    }

    @Override
    public boolean hasProperty(String property) {
        return config.hasProperty(property);
    }

    @Override
    public String getString(String property) {
        return config.getString(property);
    }

    @Override
    public Integer getInt(String property) {
        return config.getInt(property);
    }

    @Override
    public Boolean getBoolean(String property) {
        return config.getBoolean(property);
    }

    @Override
    public Double getDouble(String property) {
        return config.getDouble(property);
    }

    @Override
    public Long getLong(String property) {
        return config.getLong(property);
    }

    @Override
    public List<String> getStringList(String property) {
        return config.getStringList(property);
    }

    @Override
    public Config getGlobal() {
        return config.getGlobal();
    }

    @Override
    public Set<String> keySet() {
        return config.keySet();
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        config.addChangeListener(listener);
    }

    @Override
    public void onChange(Config oldConfig, Config newConfig) {
        this.config = newConfig;
    }
}
