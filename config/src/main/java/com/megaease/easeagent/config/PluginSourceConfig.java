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

package com.megaease.easeagent.config;

import java.util.*;

public class PluginSourceConfig {
    private final String domain;
    private final String namespace;
    private final String id;
    private final Map<String, String> source;
    private final Map<PluginProperty, String> properties;

    public PluginSourceConfig(String domain, String namespace, String id, Map<String, String> source, Map<PluginProperty, String> properties) {
        this.domain = Objects.requireNonNull(domain, "domain must not be null.");
        this.namespace = Objects.requireNonNull(namespace, "namespace must not be null.");
        this.id = Objects.requireNonNull(id, "id must not be null.");
        this.source = Objects.requireNonNull(source, "source must not be null.");
        this.properties = Objects.requireNonNull(properties, "properties must not be null.");
    }

    public static PluginSourceConfig build(String domain, String namespace, String id, Map<String, String> source) {
        Map<String, String> pluginSource = new HashMap<>();
        Map<PluginProperty, String> properties = new HashMap<>();
        for (Map.Entry<String, String> sourceEntry : source.entrySet()) {
            String key = sourceEntry.getKey();
            if (!ConfigUtils.isPluginConfig(key, domain, namespace, id)) {
                continue;
            }
            pluginSource.put(key, sourceEntry.getValue());
            PluginProperty property = ConfigUtils.pluginProperty(key);
            properties.put(property, sourceEntry.getValue());
        }
        return new PluginSourceConfig(domain, namespace, id, pluginSource, properties);
    }

    public Map<String, String> getSource() {
        return source;
    }

    public String getDomain() {
        return domain;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getId() {
        return id;
    }

    public Map<String, String> getProperties() {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<PluginProperty, String> propertyEntry : properties.entrySet()) {
            result.put(propertyEntry.getKey().getProperty(), propertyEntry.getValue());
        }
        return result;
    }
}
