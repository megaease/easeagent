package com.megaease.easeagent.config;

import java.util.*;

public class PluginSourceConfig {
    private final String domain;
    private final String namespace;
    private final Map<String, String> source;
    private final Map<String, Map<PluginProperty, String>> idProperties;

    public PluginSourceConfig(String domain, String namespace, Map<String, String> source, Map<String, Map<PluginProperty, String>> idProperties) {
        this.domain = Objects.requireNonNull(domain, "domain must not be null.");
        this.namespace = Objects.requireNonNull(namespace, "namespace must not be null.");
        this.source = Objects.requireNonNull(source, "source must not be null.");
        this.idProperties = Objects.requireNonNull(idProperties, "idProperties must not be null.");
    }

    public static PluginSourceConfig build(String domain, String namespace, Map<String, String> source) {
        Map<String, String> pluginSource = new HashMap<>();
        Map<String, Map<PluginProperty, String>> idProperties = new HashMap<>();
        for (Map.Entry<String, String> sourceEntry : source.entrySet()) {
            String key = sourceEntry.getKey();
            if (!ConfigUtils.isPluginConfig(key, domain, namespace)) {
                continue;
            }
            pluginSource.put(key, sourceEntry.getValue());
            PluginProperty property = ConfigUtils.pluginProperty(key);
            Map<PluginProperty, String> idPropertyConfig = idProperties.get(property.getId());
            if (idPropertyConfig == null) {
                idPropertyConfig = new HashMap<>();
                idProperties.put(property.getId(), idPropertyConfig);
            }
            idPropertyConfig.put(property, sourceEntry.getValue());
        }
        return new PluginSourceConfig(domain, namespace, pluginSource, idProperties);
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

    public Set<String> getIds() {
        return idProperties.keySet();
    }

    public Map<String, String> getProperties(String id) {
        Map<PluginProperty, String> pluginProperties = idProperties.get(id);
        if (pluginProperties == null || pluginProperties.isEmpty()) {
            return Collections.EMPTY_MAP;
        }
        Map<String, String> properties = new HashMap<>();
        for (Map.Entry<PluginProperty, String> pluginPropertyEntry : pluginProperties.entrySet()) {
            properties.put(pluginPropertyEntry.getKey().getProperty(), pluginPropertyEntry.getValue());
        }
        return properties;
    }
}
