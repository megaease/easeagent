/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.api.config;

import java.util.List;
import java.util.Set;

/**
 * a base AutoRefreshConfig
 */
public class AutoRefreshPluginConfigImpl implements IPluginConfig, AutoRefreshPluginConfig {
    protected volatile IPluginConfig config;

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
    public IPluginConfig getGlobal() {
        return config.getGlobal();
    }

    @Override
    public Set<String> keySet() {
        return config.keySet();
    }

    @Override
    public void addChangeListener(PluginConfigChangeListener listener) {
        config.addChangeListener(listener);
    }

    @Override
    public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
        this.config = newConfig;
    }

    public IPluginConfig getConfig() {
        return config;
    }
}
