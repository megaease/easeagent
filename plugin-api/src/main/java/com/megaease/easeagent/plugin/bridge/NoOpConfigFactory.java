/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;

import java.util.*;

public class NoOpConfigFactory implements IConfigFactory {
    @Override
    public Config getConfig() {
        return NoOpConfig.INSTANCE;
    }

    @Override
    public String getConfig(String property) {
        return null;
    }

    @Override
    public String getConfig(String property, String defaultValue) {
        return defaultValue;
    }

    @Override
    public IPluginConfig getConfig(String domain, String namespace, String id) {
        return new NoOpIPluginConfig(domain, namespace, id);
    }

    static class NoOpConfig implements Config {
        static final NoOpConfig INSTANCE = new NoOpConfig();
        private final HashMap<String, String> source = new HashMap<>();

        @Override
        public boolean hasPath(String path) {
            return false;
        }

        @Override
        public String getString(String name) {
            return null;
        }

        @Override
        public String getString(String name, String defVal) {
            return defVal;
        }

        @Override
        public Integer getInt(String name) {
            return null;
        }

        @Override
        public Integer getInt(String name, int defValue) {
            Integer anInt = getInt(name);
            if (anInt == null) {
                return defValue;
            }
            return anInt;
        }

        @Override
        public Boolean getBoolean(String name) {
            return false;
        }

        @Override
        public Boolean getBoolean(String name, boolean defValue) {
            Boolean aBoolean = getBoolean(name);
            if (aBoolean == null) {
                return defValue;
            }
            return aBoolean;
        }

        @Override
        public Boolean getBooleanNullForUnset(String name) {
            return null;
        }

        @Override
        public Double getDouble(String name) {
            return null;
        }

        @Override
        public Double getDouble(String name, double defValue) {
            return defValue;
        }

        @Override
        public Long getLong(String name) {
            return null;
        }

        @Override
        public Long getLong(String name, long defValue) {
            return defValue;
        }

        @Override
        public List<String> getStringList(String name) {
            return Collections.emptyList();
        }

        @Override
        public Runnable addChangeListener(ConfigChangeListener listener) {
            return null;
        }

        @Override
        public Set<String> keySet() {
            return this.source.keySet();
        }

        @Override
        public Map<String, String> getConfigs() {
            return source;
        }

        @Override
        public void updateConfigs(Map<String, String> changes) {
            // ignored
        }

        @Override
        public void updateConfigsNotNotify(Map<String, String> changes) {
            // ignored
        }
    }
}
