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

package com.megaease.easeagent.plugin.api.config;

import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class AutoRefreshRegistry {
    private static final AutoRefreshConfigSupplier<AutoRefreshConfigImpl> AUTO_REFRESH_CONFIG_IMPL_SUPPLIER
        = new AutoRefreshConfigSupplier<AutoRefreshConfigImpl>() {
        @Override
        public AutoRefreshConfigImpl newInstance() {
            return new AutoRefreshConfigImpl();
        }
    };

    private static final ConcurrentMap<Key, AutoRefreshConfig> configs = new ConcurrentHashMap<>();

    /**
     * Obtain an AutoRefreshConfigImpl when it is already registered. If you have not registered, create one and return
     * The registered {@link Key} is domain, namespace, id.
     *
     * @param domain    String
     * @param namespace String
     * @param id        String
     * @return AutoRefreshConfigImpl
     */
    public static AutoRefreshConfigImpl getOrCreate(String domain, String namespace, String id) {
        return getOrCreate(domain, namespace, id, AUTO_REFRESH_CONFIG_IMPL_SUPPLIER);
    }

    /**
     * Obtain an AutoRefreshConfig when it is already registered. If you have not registered, create one and return
     * The registered {@link Key} is domain, namespace, id and the type by the supplier.
     *
     * @param domain    String
     * @param namespace String
     * @param id        String
     * @param supplier  {@link AutoRefreshConfigSupplier} Instance Supplier
     * @param <C>       the type of AutoRefreshConfig by the Supplier
     * @return the type of AutoRefreshConfig by the Supplier
     */
    @SuppressWarnings("unchecked")
    public static <C extends AutoRefreshConfig> C getOrCreate(String domain, String namespace,
                                                              String id, AutoRefreshConfigSupplier<C> supplier) {
        Key key = new Key(domain, namespace, id, supplier.getType());
        AutoRefreshConfig autoRefreshConfig = configs.get(key);
        if (autoRefreshConfig != null) {
            return (C) autoRefreshConfig;
        }
        synchronized (configs) {
            autoRefreshConfig = configs.get(key);
            if (autoRefreshConfig != null) {
                return (C) autoRefreshConfig;
            }
            C newConfig = supplier.newInstance();
            Config config = EaseAgent.getConfig(domain, namespace, id);
            newConfig.onChange(null, config);
            config.addChangeListener(newConfig);
            return newConfig;
        }
    }

    static class Key {
        private final String domain;

        private final String namespace;

        private final String id;

        private final Type type;

        public Key(String domain, String namespace, String id, Type type) {
            this.domain = domain;
            this.namespace = namespace;
            this.id = id;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return Objects.equals(domain, key.domain) &&
                Objects.equals(namespace, key.namespace) &&
                Objects.equals(id, key.id) &&
                Objects.equals(type, key.type);
        }

        @Override
        public int hashCode() {

            return Objects.hash(domain, namespace, id, type);
        }

        @Override
        public String toString() {
            return "Key{" +
                "domain='" + domain + '\'' +
                ", namespace='" + namespace + '\'' +
                ", id='" + id + '\'' +
                ", type=" + type +
                '}';
        }
    }
}
