package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.IConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.megaease.easeagent.config.ConfigConst.PLUGIN_SELF;

public class PluginConfigContext implements IConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigContext.class);
    private volatile Runnable shutdownRunnable;
    private final Configs configs;
    private final Map<SourceKey, PluginSourceConfig> pluginSourceConfigs;
    private final Map<Key, PluginConfig> pluginConfigs;

    private PluginConfigContext(Configs configs, Map<SourceKey, PluginSourceConfig> pluginSourceConfigs, Map<Key, PluginConfig> pluginConfigs) {
        this.configs = Objects.requireNonNull(configs, "configs must not be null.");
        this.pluginSourceConfigs = Objects.requireNonNull(pluginSourceConfigs, "pluginSourceConfigs must not be null.");
        this.pluginConfigs = Objects.requireNonNull(pluginConfigs, "pluginConfigs must not be null.");
    }

    public static PluginConfigContext.Builder builder(Configs configs) {
        PluginConfigContext pluginConfigContext = new PluginConfigContext(configs, new HashMap<>(), new HashMap<>());
        return pluginConfigContext.new Builder();
    }

    private void validateConfigDependency() {
        for (PluginSourceConfig sourceConfigs : pluginSourceConfigs.values()) {
            Stack<String> dependency = new Stack<>();
            validateConfigDependency(dependency, sourceConfigs);
        }
    }

    private void validateConfigDependency(Stack<String> dependency, PluginSourceConfig sourceConfigs) {
        //validate dependency
        String name = sourceConfigs.getNamespace();
        if (dependency.contains(name)) {
            throw new ValidateUtils.ValidException(String.format("The plugin configuration cannot be cyclically dependent[%s->%s]", String.join("->", dependency), name));
        }
        dependency.push(sourceConfigs.getNamespace());
        for (String id : sourceConfigs.getIds()) {
            if (ConfigUtils.isSelf(id)) {
                continue;
            }
            PluginSourceConfig otherConfig = pluginSourceConfigs.get(new SourceKey(sourceConfigs.getDomain(), id));
            if (otherConfig == null) {
                continue;
            }
            validateConfigDependency(dependency, otherConfig);
        }
        dependency.pop();
    }


    @Override
    public String getConfig(String property) {
        return configs.getString(property);
    }

    public PluginConfig getConfig(String domain, String namespace, String id) {
        String pluginId = id == null ? null : id.trim();
        if (pluginId == null || pluginId.isEmpty() || id.equals(namespace)) {
            pluginId = PLUGIN_SELF;
        }
        Key key = new Key(domain, namespace, pluginId);
        PluginConfig pluginConfig = pluginConfigs.get(key);
        if (pluginConfig != null) {
            return pluginConfig;
        }
        synchronized (this) {
            pluginConfig = pluginConfigs.get(key);
            if (pluginConfig != null) {
                return pluginConfig;
            }
            Map<String, String> globalConfig = getGlobalConfig(domain, namespace, id);
            Map<String, String> coverConfig = getCoverConfig(domain, namespace, id);
            PluginConfig newPluginConfig = new PluginConfig(domain, id, globalConfig, namespace, coverConfig);
            pluginConfigs.put(key, newPluginConfig);
            pluginConfig = newPluginConfig;
        }
        return pluginConfig;
    }

    private Map<String, String> getGlobalConfig(String domain, String namespace, String id) {
        PluginSourceConfig globalConfig;
        if (ConfigUtils.isSelf(id)) {
            globalConfig = pluginSourceConfigs.get(new SourceKey(domain, namespace));
        } else {
            globalConfig = pluginSourceConfigs.get(new SourceKey(domain, id));
        }
        if (globalConfig == null) {
            return Collections.EMPTY_MAP;
        }
        return globalConfig.getProperties(PLUGIN_SELF);
    }

    private Map<String, String> getCoverConfig(String domain, String namespace, String id) {
        PluginSourceConfig sourceConfig = pluginSourceConfigs.get(new SourceKey(domain, namespace));
        if (sourceConfig == null) {
            return Collections.EMPTY_MAP;
        }
        return sourceConfig.getProperties(id);
    }

    private Set<SourceKey> sourceKeys(Set<String> keys) {
        Set<SourceKey> sourceKeys = new HashSet<>();
        for (String key : keys) {
            if (!ConfigUtils.isPluginConfig(key)) {
                continue;
            }
            sourceKeys.add(sourceKey(key));
        }
        return sourceKeys;
    }

    private Set<Key> keys(Set<String> keys) {
        Set<Key> propertyKeys = new HashSet<>();
        for (String k : keys) {
            PluginProperty property = ConfigUtils.pluginProperty(k);
            Key key = new Key(property.getDomain(), property.getNamespace(), property.getId());
            propertyKeys.add(key);
        }
        return propertyKeys;
    }

    private SourceKey sourceKey(String path) {
        PluginProperty property = ConfigUtils.pluginProperty(path);
        return new SourceKey(property.getDomain(), property.getNamespace());
    }

    public void shutdown() {
        shutdownRunnable.run();
    }

    private synchronized void onChange(Map<String, String> sources) {
        Set<SourceKey> sourceKeys = sourceKeys(sources.keySet());
        Map<String, String> newSources = new HashMap<>();
        for (SourceKey sourceKey : sourceKeys) {
            PluginSourceConfig pluginSourceConfig = pluginSourceConfigs.get(sourceKey);
            if (pluginSourceConfig == null) {
                continue;
            }
            newSources.putAll(pluginSourceConfig.getSource());
        }
        newSources.putAll(sources);
        for (SourceKey sourceKey : sourceKeys) {
            pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.domain, sourceKey.namespace, newSources));
        }
        Set<Key> keys = keys(sources.keySet());
        Set<Key> changeKeys = new HashSet<>(keys);
        for (Key key : keys) {
            if (!ConfigUtils.isSelf(key.id)) {
                continue;
            }
            for (Key oldKey : pluginConfigs.keySet()) {
                if (key.getNamespace().equals(oldKey.id)) {
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

    class SourceKey {
        private final String domain;
        private final String namespace;

        public SourceKey(String domain, String namespace) {
            this.domain = domain;
            this.namespace = namespace;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SourceKey sourceKey = (SourceKey) o;
            return Objects.equals(domain, sourceKey.domain) &&
                Objects.equals(namespace, sourceKey.namespace);
        }

        public String getDomain() {
            return domain;
        }

        public String getNamespace() {
            return namespace;
        }

        @Override
        public int hashCode() {

            return Objects.hash(domain, namespace);
        }
    }

    class Key extends SourceKey {
        private final String id;

        public Key(String domain, String namespace, String id) {
            super(domain, namespace);
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            Key key = (Key) o;
            return Objects.equals(id, key.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), id);
        }
    }

    public class Builder {
        public PluginConfigContext build() {
            synchronized (PluginConfigContext.this) {
                Map<String, String> sources = configs.getConfigs();
                Set<SourceKey> sourceKeys = sourceKeys(sources.keySet());
                for (SourceKey sourceKey : sourceKeys) {
                    pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.domain, sourceKey.namespace, sources));
                }
                validateConfigDependency();
                for (PluginSourceConfig sourceConfig : pluginSourceConfigs.values()) {
                    String domain = sourceConfig.getDomain();
                    String namespace = sourceConfig.getNamespace();
                    for (String id : sourceConfig.getIds()) {
                        getConfig(domain, namespace, id);
                    }
                }
                shutdownRunnable = configs.addChangeListener(new ChangeListener());
            }
            return PluginConfigContext.this;
        }
    }

    class ChangeListener implements com.megaease.easeagent.config.ConfigChangeListener {

        @Override
        public void onChange(List<ChangeItem> list) {
            Map<String, String> sources = new HashMap<>();
            for (ChangeItem changeItem : list) {
                sources.put(changeItem.getFullName(), changeItem.getNewValue());
            }
            PluginConfigContext.this.onChange(sources);
        }
    }
}
