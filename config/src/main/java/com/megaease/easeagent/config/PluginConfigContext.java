package com.megaease.easeagent.config;

import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.megaease.easeagent.config.ConfigConst.*;

public class PluginConfigContext implements com.megaease.easeagent.config.ConfigChangeListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigContext.class);
    private final CopyOnWriteArrayList<ConfigChangeListener> listeners = new CopyOnWriteArrayList<>();
    private Runnable shutdownRunnable;
    private final Configs configs;
    private final Map<SourceKey, PluginSourceConfig> pluginSourceConfigs;
    private final Map<Key, PluginConfig> pluginConfigs;

    public PluginConfigContext(Configs configs, Map<SourceKey, PluginSourceConfig> pluginSourceConfigs, Map<Key, PluginConfig> pluginConfigs) {
        this.configs = Objects.requireNonNull(configs, "configs must not be null.");
        this.pluginSourceConfigs = Objects.requireNonNull(pluginSourceConfigs, "pluginSourceConfigs must not be null.");
        this.pluginConfigs = Objects.requireNonNull(pluginConfigs, "pluginConfigs must not be null.");
    }


    public PluginConfigContext build(Configs configs) {
        Map<String, String> sources = configs.getConfigs();
        Set<SourceKey> pluginNamespaces = new HashSet<>();
        for (Map.Entry<String, String> source : sources.entrySet()) {
            String key = source.getKey();
            if (!ConfigUtils.isPluginConfig(key)) {
                continue;
            }
            pluginNamespaces.add(sourceKey(key));
        }
        Map<SourceKey, PluginSourceConfig> pluginSourceConfigs = new HashMap<>();
        for (SourceKey sourceKey : pluginNamespaces) {
            pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.domain, sourceKey.namespace, sources));
        }
        validateConfigDependency(pluginSourceConfigs);
        Map<Key, PluginConfig> pluginConfigs = new HashMap<>();
        for (PluginSourceConfig sourceConfig : pluginSourceConfigs.values()) {
            String domain = sourceConfig.getDomain();
            String namespace = sourceConfig.getNamespace();
            for (String id : sourceConfig.getIds()) {
                Key key = new Key(domain, namespace, id);
                PluginSourceConfig globalConfig;
                if (ConfigUtils.isSelf(id)) {
                    globalConfig = sourceConfig;
                } else {
                    globalConfig = pluginSourceConfigs.get(new SourceKey(domain, id));
                }
                PluginConfig pluginConfig = new PluginConfig(domain, id, globalConfig.getProperties(PLUGIN_SELF), namespace, sourceConfig.getProperties(id), this);
                pluginConfigs.put(key, pluginConfig);
            }

        }
        PluginConfigContext pluginConfigContext = new PluginConfigContext(configs, pluginSourceConfigs, pluginConfigs);
        pluginConfigContext.shutdownRunnable = configs.addChangeListener(pluginConfigContext);
        return pluginConfigContext;
    }

    private void validateConfigDependency(Map<SourceKey, PluginSourceConfig> pluginSourceConfigs) {
        for (PluginSourceConfig sourceConfigs : pluginSourceConfigs.values()) {
            Stack<String> dependency = new Stack<>();
            validateConfigDependency(dependency, sourceConfigs, pluginSourceConfigs);
        }
    }

    private void validateConfigDependency(Stack<String> dependency, PluginSourceConfig sourceConfigs, Map<SourceKey, PluginSourceConfig> pluginSourceConfigs) {
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
            PluginSourceConfig otherConfig = pluginSourceConfigs.get(id);
            if (otherConfig == null) {
                continue;
            }
            validateConfigDependency(dependency, otherConfig, pluginSourceConfigs);
        }
        dependency.pop();
    }


    public PluginConfig getConfig(String domain, String namespace, String id) {
        String pluginId = id;
        if (pluginId == null || pluginId.trim().isEmpty()) {
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
            PluginConfig newPluginConfig = new PluginConfig(domain, id, globalConfig, namespace, coverConfig, this);
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


    public Runnable addChangeListener(ConfigChangeListener listener) {
        final boolean add = listeners.add(listener);
        return () -> {
            if (add) {
                listeners.remove(listener);
            }
        };
    }


    private SourceKey sourceKey(String path) {
        PluginProperty property = ConfigUtils.pluginProperty(path);
        return new SourceKey(property.getDomain(), property.getNamespace());
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        //todo onChange
    }


    public void shutdown() {
        shutdownRunnable.run();
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
}
