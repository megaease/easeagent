package com.megaease.easeagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Configs implements Config, ConfigManagerMXBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private volatile Map<String, String> source;
    private final ConfigNotifier notifier;
    private volatile String mainLatestVersion;
    private volatile String canaryLatestVersion;

    public Configs(Map<String, String> source) {
        this.source = new HashMap<>(source);
        notifier = new ConfigNotifier("");
    }

    public void updateConfigsNotNotify(Map<String, String> changes) {
        this.source.putAll(changes);
    }

    public void updateConfigs(Map<String, String> changes) {
        Map<String, String> dump = new HashMap<>(this.source);
        List<ChangeItem> items = new LinkedList<>();
        changes.forEach((name, value) -> {
            String old = dump.get(name);
            if (!Objects.equals(old, value)) {
                dump.put(name, value);
                items.add(new ChangeItem(name, name, old, value));
            }
        });
        if (!items.isEmpty()) {
            logger.info("change items: {}", items);
            this.source = dump;
            this.notifier.handleChanges(items);
        }
    }

    @Override
    public void updateService(String json, String version) throws IOException {
        logger.info("call updateService. version: {}, json: {}", version, json);
        if (hasText(mainLatestVersion) && Objects.equals(mainLatestVersion, version)) {
            logger.info("new main version: {} is same with the old version: {}", version, mainLatestVersion);
            return;
        } else if (hasText(version)) {
            logger.info("update the main latest version to {}", version);
            this.mainLatestVersion = version;
        }
        this.updateConfigs(ConfigUtils.json2KVMap(json));
    }

    @Override
    public void updateCanary(String json, String version) throws IOException {
        logger.info("call updateCanary. version: {}, json: {}", version, json);
        if (hasText(canaryLatestVersion) && Objects.equals(canaryLatestVersion, version)) {
            logger.info("new canary version: {} is same with the old version: {}", version, canaryLatestVersion);
            return;
        } else if (hasText(version)) {
            logger.info("update the canary latest version to {}", version);
            this.canaryLatestVersion = version;
        }
        Map<String, String> originals = ConfigUtils.json2KVMap(json);
        HashMap<String, String> rst = new HashMap<>();
        originals.forEach((k, v) -> rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v));
        this.updateConfigs(rst);
    }

    @Override
    public void updateService(Map<String, String> configs, String version) {
        logger.info("call updateService. version: {}, configs: {}", version, configs);
        if (hasText(mainLatestVersion) && Objects.equals(mainLatestVersion, version)) {
            logger.info("new main version: {} is same with the old version: {}", version, mainLatestVersion);
        }
        if (hasText(version)) {
            logger.info("update the main latest version to {}", version);
            this.mainLatestVersion = version;
        }
        this.updateConfigs(configs);
    }

    @Override
    public void updateCanary(Map<String, String> configs, String version) {
        logger.info("call updateCanary. version: {}, configs: {}", version, configs);
        if (hasText(canaryLatestVersion) && Objects.equals(canaryLatestVersion, version)) {
            logger.info("new canary version: {} is same with the old version: {}", version, canaryLatestVersion);
            return;
        } else if (hasText(version)) {
            logger.info("update the canary latest version to {}", version);
            this.canaryLatestVersion = version;
        }
        HashMap<String, String> rst = new HashMap<>();
        configs.forEach((k, v) -> rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v));
        this.updateConfigs(rst);
    }

    private boolean hasText(String text) {
        return text != null && text.trim().length() > 0;
    }

    @Override
    public Map<String, String> getConfigs() {
        return new HashMap<>(this.source);
    }

    @Override
    public List<String> availableConfigNames() {
        throw new UnsupportedOperationException();
    }

    public String toPrettyDisplay() {
        return this.source.toString();
    }

    public boolean hasPath(String path) {
        return this.source.containsKey(path);
    }


    public String getString(String name) {
        return this.source.get(name);
    }

    public Integer getInt(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean getBoolean(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return false;
        }
        return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
    }

    public Double getDouble(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    public Long getLong(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> getStringList(String name) {
        String value = this.source.get(name);
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(value.split(",")).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Runnable addChangeListener(ConfigChangeListener listener) {
        return notifier.addChangeListener(listener);
    }

    @Override
    public Set<String> keySet() {
        return this.source.keySet();
    }
}
