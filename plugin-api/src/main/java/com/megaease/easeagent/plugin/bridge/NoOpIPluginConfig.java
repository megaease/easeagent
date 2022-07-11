/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NoOpIPluginConfig implements IPluginConfig {
    public static NoOpIPluginConfig INSTANCE = new NoOpIPluginConfig("Noop", "Noop", "Noop");
    private final String domain;
    private final String namespace;
    private final String id;

    public NoOpIPluginConfig(String domain, String namespace, String id) {
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
    public boolean enabled() {
        return false;
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
    public IPluginConfig getGlobal() {
        return this;
    }

    @Override
    public void addChangeListener(PluginConfigChangeListener listener) {

    }

    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }
}
