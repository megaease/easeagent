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

package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceMetricRegistry {
    public static final ConcurrentHashMap<Key, ServiceMetric> INSTANCES = new ConcurrentHashMap<>();

    /**
     * Obtain an ServiceMetric when it is already registered. If you have not registered, create one and return
     * The registered {@link Key} is domain, namespace, id, tags and the type by the supplier.
     *
     * @param config              {@link IPluginConfig} domain, namespace from id from
     * @param tags                {@link Tags} metric tags
     * @param supplier            {@link ServiceMetric} Instance Supplier
     * @param <T>                 the type of ServiceMetric by the Supplier
     * @return the type of ServiceMetric by the Supplier
     */
    public static <T extends ServiceMetric> T getOrCreate(IPluginConfig config, Tags tags, ServiceMetricSupplier<T> supplier) {
        return getOrCreate(config.domain(), config.namespace(), config.id(), tags, supplier);
    }

    /**
     * Obtain an ServiceMetric when it is already registered. If you have not registered, create one and return
     * The registered {@link Key} is domain, namespace, id, tags and the type by the supplier.
     *
     * @param domain              String
     * @param namespace           String
     * @param id                  String
     * @param tags                {@link Tags} metric tags
     * @param supplier            {@link ServiceMetric} Instance Supplier
     * @param <T>                 the type of ServiceMetric by the Supplier
     * @return the type of ServiceMetric by the Supplier
     */
    @SuppressWarnings("unchecked")
    public static <T extends ServiceMetric> T getOrCreate(String domain, String namespace, String id, Tags tags, ServiceMetricSupplier<T> supplier) {
        Key key = new Key(domain, namespace, id, tags, supplier.getType());
        ServiceMetric metric = INSTANCES.get(key);
        if (metric != null) {
            return (T) metric;
        }
        synchronized (INSTANCES) {
            metric = INSTANCES.get(key);
            if (metric != null) {
                return (T) metric;
            }
            IPluginConfig config = EaseAgent.getConfig(domain, namespace, id);
            NameFactory nameFactory = supplier.newNameFactory();
            MetricRegistry metricRegistry = EaseAgent.newMetricRegistry(config, nameFactory, tags);
            T newMetric = supplier.newInstance(metricRegistry, nameFactory);
            INSTANCES.put(key, newMetric);
            return newMetric;
        }
    }

    static class Key {
        private final int hash;
        private final String domain;
        private final String namespace;
        private final String id;
        private final Tags tags;
        private final Type type;

        public Key(String domain, String namespace, String id, Tags tags, Type type) {
            this.domain = domain;
            this.namespace = namespace;
            this.id = id;
            this.tags = tags;
            this.type = type;
            this.hash = Objects.hash(domain, namespace, id, tags, type);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Key key = (Key) o;
            return hash == key.hash &&
                Objects.equals(domain, key.domain) &&
                Objects.equals(namespace, key.namespace) &&
                Objects.equals(id, key.id) &&
                Objects.equals(tags, key.tags) &&
                Objects.equals(type, key.type);
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public String toString() {
            return "Key{" +
                "hash=" + hash +
                ", domain='" + domain + '\'' +
                ", namespace='" + namespace + '\'' +
                ", id='" + id + '\'' +
                ", tags=" + tags +
                ", type=" + type +
                '}';
        }
    }
}
