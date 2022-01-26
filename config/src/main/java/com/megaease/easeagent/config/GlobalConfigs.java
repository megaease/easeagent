package com.megaease.easeagent.config;

import com.megaease.easeagent.config.report.ReporterConfigAdapter;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GlobalConfigs extends Configs implements ConfigManagerMXBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalConfigs.class);

    private volatile String mainLatestVersion;
    private volatile String canaryLatestVersion;

    public GlobalConfigs(Map<String, String> source) {
        super();
        // reporter adapter
        Map<String, String> map = new HashMap<>(source);
        ReporterConfigAdapter.convertConfig(map);
        this.source = new HashMap<>(source);
        this.notifier = new ConfigNotifier("");
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // report adapter
        Map<String, String> changesMap = new HashMap<>(changes);
        ReporterConfigAdapter.convertConfig(changesMap);

        super.updateConfigs(changesMap);
    }

    @Override
    public List<String> availableConfigNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateService(String json, String version) throws IOException {
        LOGGER.info("call updateService. version: {}, json: {}", version, json);
        if (hasText(mainLatestVersion) && Objects.equals(mainLatestVersion, version)) {
            LOGGER.info("new main version: {} is same with the old version: {}", version, mainLatestVersion);
            return;
        } else if (hasText(version)) {
            LOGGER.info("update the main latest version to {}", version);
            this.mainLatestVersion = version;
        }
        this.updateConfigs(ConfigUtils.json2KVMap(json));
    }

    @Override
    public void updateCanary(String json, String version) throws IOException {
        LOGGER.info("call updateCanary. version: {}, json: {}", version, json);
        if (hasText(canaryLatestVersion) && Objects.equals(canaryLatestVersion, version)) {
            LOGGER.info("new canary version: {} is same with the old version: {}", version, canaryLatestVersion);
            return;
        } else if (hasText(version)) {
            LOGGER.info("update the canary latest version to {}", version);
            this.canaryLatestVersion = version;
        }
        Map<String, String> originals = ConfigUtils.json2KVMap(json);
        HashMap<String, String> rst = new HashMap<>();
        originals.forEach((k, v) -> rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v));
        this.updateConfigs(rst);
    }

    @Override
    public void updateService2(Map<String, String> configs, String version) {
        LOGGER.info("call updateService. version: {}, configs: {}", version, configs);
        if (hasText(mainLatestVersion) && Objects.equals(mainLatestVersion, version)) {
            LOGGER.info("new main version: {} is same with the old version: {}", version, mainLatestVersion);
        }
        if (hasText(version)) {
            LOGGER.info("update the main latest version to {}", version);
            this.mainLatestVersion = version;
        }
        this.updateConfigs(configs);
    }

    @Override
    public void updateCanary2(Map<String, String> configs, String version) {
        LOGGER.info("call updateCanary. version: {}, configs: {}", version, configs);
        if (hasText(canaryLatestVersion) && Objects.equals(canaryLatestVersion, version)) {
            LOGGER.info("new canary version: {} is same with the old version: {}", version, canaryLatestVersion);
            return;
        } else if (hasText(version)) {
            LOGGER.info("update the canary latest version to {}", version);
            this.canaryLatestVersion = version;
        }
        HashMap<String, String> rst = new HashMap<>();
        configs.forEach((k, v) -> rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v));
        this.updateConfigs(CompatibilityConversion.transform(rst));
    }
}
