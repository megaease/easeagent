/*
 * Copyright (c) 2022, MegaEase
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

package com.megaease.easeagent.mock.config;

import com.megaease.easeagent.config.ConfigFactory;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.config.GlobalConfigs;
import com.megaease.easeagent.config.PluginConfigManager;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MockConfig {
    private static final String MOCK_CONFIG_YAML_FILE = "mock_agent.yaml";
    private static final String MOCK_CONFIG_PROP_FILE = "mock_agent.properties";
    private static final GlobalConfigs CONFIGS;
    private static final PluginConfigManager PLUGIN_CONFIG_MANAGER;

    static {
        Map<String, String> initConfigs = new HashMap<>();
        initConfigs.put("name", "demo-service");
        initConfigs.put("system", "demo-system");

        initConfigs.put("observability.outputServer.timeout", "10000");
        initConfigs.put("observability.outputServer.enabled", "true");
        initConfigs.put("observability.tracings.output.enabled", "true");
        initConfigs.put("plugin.observability.global.tracing.enabled", "true");
        initConfigs.put("plugin.observability.global.metric.enabled", "true");
        CONFIGS = new GlobalConfigs(initConfigs);
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL url = classLoader.getResource(MOCK_CONFIG_YAML_FILE);
        if (url == null) {
             url = classLoader.getResource(MOCK_CONFIG_PROP_FILE);
        }
        if (url != null) {
            GlobalConfigs configsFromOuterFile = ConfigFactory.loadFromFile(new File(url.getFile()));
            CONFIGS.mergeConfigs(configsFromOuterFile);
        }
        PLUGIN_CONFIG_MANAGER = PluginConfigManager.builder(CONFIGS).build();
    }

    public static Configs getCONFIGS() {
        return CONFIGS;
    }

    public static PluginConfigManager getPluginConfigManager() {
        return PLUGIN_CONFIG_MANAGER;
    }
}
