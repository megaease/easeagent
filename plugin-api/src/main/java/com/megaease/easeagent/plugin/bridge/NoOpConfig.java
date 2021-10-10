package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NoOpConfig implements Config {
    private final String domain;
    private final String namespace;
    private final String id;

    public NoOpConfig(String domain, String namespace, String id) {
        this.domain = domain;
        this.namespace = namespace;
        this.id = id;
    }


    @Override
    public String domain() {
        return domain;
    }

    @Override
    public String namespace() {
        return namespace;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public boolean hasProperty(String property) {
        return false;
    }

    @Override
    public String getString(String property) {
        return null;
    }

    @Override
    public Integer getInt(String property) {
        return null;
    }

    @Override
    public Boolean getBoolean(String property) {
        return null;
    }

    @Override
    public Double getDouble(String property) {
        return null;
    }

    @Override
    public Long getLong(String property) {
        return null;
    }

    @Override
    public List<String> getStringList(String property) {
        return Collections.emptyList();
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {

    }

    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }
}
