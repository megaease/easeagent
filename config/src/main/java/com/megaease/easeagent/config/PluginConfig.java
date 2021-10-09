package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import java.util.*;
import java.util.stream.Collectors;

public class PluginConfig implements Config {
    private final PluginConfigContext pluginConfigContext;
    private final String domain;
    private final String namespace;
    private final String id;
    private final Map<String, String> global;
    private final Map<String, String> cover;

    public PluginConfig(String domain, String id, Map<String, String> global, String namespace, Map<String, String> cover, PluginConfigContext pluginConfigContext) {
        this.domain = Objects.requireNonNull(domain, "domain must not be null.");
        this.namespace = Objects.requireNonNull(namespace, "namespace must not be null.");
        this.id = Objects.requireNonNull(id, "id must not be null.");
        this.global = Objects.requireNonNull(global, "global must not be null.");
        this.cover = Objects.requireNonNull(cover, "cover must not be null.");
        this.pluginConfigContext = Objects.requireNonNull(pluginConfigContext, "pluginConfigContext must not be null.");
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
        return global.containsKey(property) || cover.containsKey(property);
    }

    @Override
    public String getString(String property) {
        String value = cover.get(property);
        if (value != null) {
            return value;
        }
        return global.get(property);
    }


    @Override
    public Integer getInt(String property) {
        String value = this.getString(property);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isTrue(String value) {
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    @Override
    public Boolean getBoolean(String property) {
        String value = cover.get(property);
        boolean implB = true;
        if (value != null) {
            implB = isTrue(value);
        }
        value = global.get(property);
        boolean globalB = false;
        if (value != null) {
            globalB = isTrue(value);
        }
        return implB && globalB;
    }

    @Override
    public Double getDouble(String property) {
        String value = this.getString(property);
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Long getLong(String property) {
        String value = this.getString(property);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> getStringList(String property) {
        String value = this.getString(property);
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(",")).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Runnable addChangeListener(ConfigChangeListener listener) {
        return pluginConfigContext.addChangeListener(listener);
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>(global.keySet());
        keys.addAll(cover.keySet());
        return keys;
    }

}
