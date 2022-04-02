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
import com.megaease.easeagent.plugin.api.config.ConfigConst;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GlobalConfigs extends Configs implements ConfigManagerMXBean {
    Configs originalConfig;

    public GlobalConfigs(Map<String, String> source) {
        super();
        this.originalConfig = new Configs(source);
        // reporter adapter
        Map<String, String> map = new TreeMap<>(source);
        ReportConfigAdapter.convertConfig(map);
        // check environment config
        this.source = new TreeMap<>(map);
        this.notifier = new ConfigNotifier("");
    }

    public Configs getOriginalConfig() {
        return this.originalConfig;
    }

    @Override
    public void updateConfigsNotNotify(Map<String, String> changes) {
        // update original config
        Map<String, String> newGlobalCfg = new TreeMap<>(this.originalConfig.getConfigs());
        newGlobalCfg.putAll(changes);
        this.originalConfig.updateConfigsNotNotify(changes);

        // report adapter
        ReportConfigAdapter.convertConfig(newGlobalCfg);

        super.updateConfigsNotNotify(newGlobalCfg);
    }

    @Override
    public void updateConfigs(Map<String, String> changes) {
        // update original config
        Map<String, String> newGlobalCfg = new TreeMap<>(this.originalConfig.getConfigs());
        newGlobalCfg.putAll(changes);
        this.originalConfig.updateConfigsNotNotify(changes);

        // report adapter
        ReportConfigAdapter.convertConfig(newGlobalCfg);

        super.updateConfigs(newGlobalCfg);
    }

    public void mergeConfigs(GlobalConfigs configs) {
        Map<String, String> merged = configs.getOriginalConfig().getConfigs();
        this.updateConfigsNotNotify(merged);
        return;
    }

    @Override
    public List<String> availableConfigNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateService(String json, String version) throws IOException {
        this.updateConfigs(ConfigUtils.json2KVMap(json));
    }

    @Override
    public void updateCanary(String json, String version) throws IOException {
        Map<String, String> originals = ConfigUtils.json2KVMap(json);
        HashMap<String, String> rst = new HashMap<>();
        originals.forEach((k, v) -> rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v));
        this.updateConfigs(rst);
    }

    @Override
    public void updateService2(Map<String, String> configs, String version) {
        this.updateConfigs(configs);
    }

    @Override
    public void updateCanary2(Map<String, String> configs, String version) {
        HashMap<String, String> rst = new HashMap<>();
        for (Map.Entry<String, String> entry : configs.entrySet()) {
            String k = entry.getKey();
            String v = entry.getValue();
            rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v);
        }
        this.updateConfigs(CompatibilityConversion.transform(rst));
    }
}
