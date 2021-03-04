package com.megaease.easeagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ConfigDelegate implements Config {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigDelegate.class);
    private final String prefix;
    private final Config delegate;
    private final ConfigNotifier notifier;

    public ConfigDelegate(Config delegate, String prefix) {
        this.delegate = delegate;
        this.prefix = prefix;
        notifier = new ConfigNotifier(this.prefix);
    }

    public String prefixedName(String name) {
        return this.prefix + name;
    }

    public boolean hasPath(String path) {
        return this.delegate.hasPath(prefixedName(path));
    }


    public String getString(String name) {
        return this.delegate.getString(prefixedName(name));
    }

    public Integer getInt(String name) {
        return this.delegate.getInt(prefixedName(name));
    }

    public Boolean getBoolean(String name) {
        return this.delegate.getBoolean(prefixedName(name));
    }

    public Double getDouble(String name) {
        return this.delegate.getDouble(prefixedName(name));
    }

    public Long getLong(String name) {
        return this.delegate.getLong(prefixedName(name));
    }

    public List<String> getStringList(String name) {
        return this.delegate.getStringList(prefixedName(name));
    }

    @Override
    public Runnable addChangeListener(ConfigChangeListener listener) {
        return notifier.addChangeListener(listener);
    }

    void handleChanges(List<ChangeItem> list) {
        notifier.handleChanges(list);
    }
}
