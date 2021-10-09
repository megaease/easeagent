package com.megaease.easeagent.plugin.api.config;

public interface ConfigChangeListener {
    void onChange(Config oldConfig, Config newConfig);
}
