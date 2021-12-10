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

package com.megaease.easeagent.plugin.tools.config;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class AutoRefreshRegistry {
    private static final ConcurrentMap<Key, AutoRefreshConfig> configs = new ConcurrentHashMap<>();

    public static BaseAutoRefreshConfig getOrCreate(String domain, String namespace, String name) {
        Config config = EaseAgent.configFactory
            .getConfig(domain, namespace, name);
        return getOrCreate(config);
    }

    public static BaseAutoRefreshConfig getOrCreate(Config config) {
        return getOrCreate(config, () -> new BaseAutoRefreshConfig());
    }

    public static <C extends AutoRefreshConfig> C getOrCreate(Config config, Supplier<C> supplier) {
        Key key = new Key(config.domain(), config.namespace(), config.id());
        AutoRefreshConfig autoRefreshConfig = configs.get(key);
        if (autoRefreshConfig != null) {
            return (C) autoRefreshConfig;
        }
        synchronized (configs) {
            autoRefreshConfig = configs.get(key);
            if (autoRefreshConfig != null) {
                return (C) autoRefreshConfig;
            }
            autoRefreshConfig = supplier.get();
            autoRefreshConfig.onChange(null, config);
            config.addChangeListener(autoRefreshConfig);
            return (C) autoRefreshConfig;
        }
    }

    static class Key {
        private final String domain;

        private final String namespace;

        private final String id;

        public Key(String domain, String namespace, String id) {
            this.domain = domain;
            this.namespace = namespace;
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(domain, key.domain) &&
                Objects.equals(namespace, key.namespace) &&
                Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {

            return Objects.hash(domain, namespace, id);
        }
    }
}
