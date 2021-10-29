package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.megaease.easeagent.config.ConfigConst.PLUGIN_GLOBAL;

public class PluginConfigManager implements IConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigManager.class);
    private volatile Runnable shutdownRunnable;
    private final Configs configs;
    private final Map<Key, PluginSourceConfig> pluginSourceConfigs;
    private final Map<Key, PluginConfig> pluginConfigs;

    private PluginConfigManager(Configs configs, Map<Key, PluginSourceConfig> pluginSourceConfigs, Map<Key, PluginConfig> pluginConfigs) {
        this.configs = Objects.requireNonNull(configs, "configs must not be null.");
        this.pluginSourceConfigs = Objects.requireNonNull(pluginSourceConfigs, "pluginSourceConfigs must not be null.");
        this.pluginConfigs = Objects.requireNonNull(pluginConfigs, "pluginConfigs must not be null.");
    }

    public static PluginConfigManager.Builder builder(Configs configs) {
        PluginConfigManager pluginConfigManager = new PluginConfigManager(configs, new HashMap<>(), new HashMap<>());
        return pluginConfigManager.new Builder();
    }

    @Override
    public String getConfig(String property) {
        return configs.getString(property);
    }

    public synchronized PluginConfig getConfig(String domain, String namespace, String id) {
        Key key = new Key(domain, namespace, id);
        PluginConfig pluginConfig = pluginConfigs.get(key);
        if (pluginConfig != null) {
            return pluginConfig;
        }
        Map<String, String> globalConfig = getGlobalConfig(domain, id);
        Map<String, String> coverConfig = getCoverConfig(domain, namespace, id);
        PluginConfig newPluginConfig = new PluginConfig(domain, id, globalConfig, namespace, coverConfig);
        pluginConfigs.put(key, newPluginConfig);
        return newPluginConfig;
    }

    private Map<String, String> getGlobalConfig(String domain, String id) {
        return getConfigSource(domain, PLUGIN_GLOBAL, id);
    }

    private Map<String, String> getCoverConfig(String domain, String namespace, String id) {
        return getConfigSource(domain, namespace, id);
    }

    private Map<String, String> getConfigSource(String domain, String namespace, String id) {
        PluginSourceConfig sourceConfig = pluginSourceConfigs.get(new Key(domain, namespace, id));
        if (sourceConfig == null) {
            return Collections.EMPTY_MAP;
        }
        return sourceConfig.getProperties();
    }


    private Set<Key> keys(Set<String> keys) {
        Set<Key> propertyKeys = new HashSet<>();
        for (String k : keys) {
            if (!ConfigUtils.isPluginConfig(k)) {
                continue;
            }
            PluginProperty property = ConfigUtils.pluginProperty(k);
            Key key = new Key(property.getDomain(), property.getNamespace(), property.getId());
            propertyKeys.add(key);
        }
        return propertyKeys;
    }


    public void shutdown() {
        shutdownRunnable.run();
    }

    private synchronized void onChange(Map<String, String> sources) {
        Set<Key> sourceKeys = keys(sources.keySet());
        Map<String, String> newSources = new HashMap<>();
        for (Key sourceKey : sourceKeys) {
            PluginSourceConfig pluginSourceConfig = pluginSourceConfigs.get(sourceKey);
            if (pluginSourceConfig == null) {
                continue;
            }
            newSources.putAll(pluginSourceConfig.getSource());
        }
        newSources.putAll(sources);
        for (Key sourceKey : sourceKeys) {
            pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.getDomain(), sourceKey.getNamespace(), sourceKey.getId(), newSources));
        }
        Set<Key> changeKeys = new HashSet<>(sourceKeys);
        for (Key key : sourceKeys) {
            if (!ConfigUtils.isGlobal(key.getNamespace())) {
                continue;
            }
            for (Key oldKey : pluginConfigs.keySet()) {
                if (key.id.equals(oldKey.id)) {
                    changeKeys.add(oldKey);
                }
            }
        }
        for (Key changeKey : changeKeys) {
            PluginConfig oldConfig = pluginConfigs.remove(changeKey);
            PluginConfig newConfig = getConfig(changeKey.getDomain(), changeKey.getNamespace(), changeKey.id);
            com.megaease.easeagent.plugin.api.config.ConfigChangeListener changeListener = oldConfig.getConfigChangeListener();
            if (changeListener == null) {
                continue;
            }
            newConfig.addChangeListener(changeListener);
            try {
                changeListener.onChange(oldConfig, newConfig);
            } catch (Exception e) {
                LOGGER.error("on change config fail: {}", e);
            }
        }
    }

    class Key {
        private final String domain;
        private final String namespace;
        private final String id;

        public Key(String domain, String namespace, String id) {
            this.domain = domain;
            this.namespace = namespace;
            this.id = id;
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

    public class Builder {
        public PluginConfigManager build() {
            synchronized (PluginConfigManager.this) {
                Map<String, String> sources = configs.getConfigs();
                Set<Key> sourceKeys = keys(sources.keySet());
                for (Key sourceKey : sourceKeys) {
                    pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.getDomain(), sourceKey.getNamespace(), sourceKey.getId(), sources));
                }
                for (Key key : pluginSourceConfigs.keySet()) {
                    getConfig(key.getDomain(), key.getNamespace(), key.getId());
                }
                shutdownRunnable = configs.addChangeListener(new ChangeListener());
            }
            return PluginConfigManager.this;
        }
    }

    class ChangeListener implements com.megaease.easeagent.config.ConfigChangeListener {

        @Override
        public void onChange(List<ChangeItem> list) {
            Map<String, String> sources = new HashMap<>();
            for (ChangeItem changeItem : list) {
                sources.put(changeItem.getFullName(), changeItem.getNewValue());
            }
            PluginConfigManager.this.onChange(sources);
        }
    }
}
