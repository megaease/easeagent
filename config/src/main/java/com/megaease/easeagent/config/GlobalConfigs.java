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
 *
 */
package com.megaease.easeagent.config;

import com.megaease.easeagent.config.report.ReportConfigAdapter;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import java.io.IOException;
import java.util.*;

public class GlobalConfigs extends Configs implements ConfigManagerMXBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalConfigs.class);

    private volatile String mainLatestVersion;
    private volatile String canaryLatestVersion;

    public GlobalConfigs(Map<String, String> source) {
        super();
        // reporter adapter
        Map<String, String> map = new TreeMap<>(source);
        ReportConfigAdapter.convertConfig(map);
        // check environment config
        this.source = new TreeMap<>(map);
        this.notifier = new ConfigNotifier("");
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // report adapter
        Map<String, String> changesMap = new HashMap<>(changes);
        ReportConfigAdapter.convertConfig(changesMap);

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
