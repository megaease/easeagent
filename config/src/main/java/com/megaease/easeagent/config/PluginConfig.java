package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

public class PluginConfig implements Config {
    private volatile ConfigChangeListener listener;
    private final String domain;
    private final String namespace;
    private final String id;
    private final Map<String, String> global;
    private final Map<String, String> cover;

    public PluginConfig(@Nonnull String domain, @Nonnull String id, @Nonnull Map<String, String> global, @Nonnull String namespace, @Nonnull Map<String, String> cover) {
        this.domain = domain;
        this.namespace = namespace;
        this.id = id;
        this.global = global;
        this.cover = cover;
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
    public Config getGlobal() {
        return new Global(domain, id, global, namespace);
    }

    @Override
    public Set<String> keySet() {
        Set<String> keys = new HashSet<>(global.keySet());
        keys.addAll(cover.keySet());
        return keys;
    }

    @Override
    public void addChangeListener(ConfigChangeListener listener) {
        this.listener = listener;
    }

    public ConfigChangeListener getConfigChangeListener() {
        return listener;
    }

    public class Global extends PluginConfig implements Config {

        public Global(String domain, String id, Map<String, String> global, String namespace) {
            super(domain, id, global, namespace, Collections.emptyMap());
        }

        @Override
        public void addChangeListener(ConfigChangeListener listener) {
            PluginConfig.this.addChangeListener(listener);
        }

        @Override
        public ConfigChangeListener getConfigChangeListener() {
            return PluginConfig.this.getConfigChangeListener();
        }
    }
}
