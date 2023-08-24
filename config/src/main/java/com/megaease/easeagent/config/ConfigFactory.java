/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.config;

import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.utils.SystemEnv;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import java.io.File;
import java.util.*;

public class ConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);
    private static final String CONFIG_PROP_FILE = "agent.properties";
    private static final String CONFIG_YAML_FILE = "agent.yaml";

    private static final String AGENT_SERVICE_NAME = "easeagent.name";
    private static final String AGENT_SYSTEM_NAME = "easeagent.system";

    private static final String AGENT_SERVER_PORT_KEY = "easeagent.server.port";
    private static final String AGENT_SERVER_ENABLED_KEY = "easeagent.server.enabled";

    public static final String EASEAGENT_ENV_CONFIG = "EASEAGENT_ENV_CONFIG";

    private static final List<String> subEnvKeys = new LinkedList<>();
    private static final List<String> envKeys = new LinkedList<>();

    static {
        subEnvKeys.add(AGENT_SERVICE_NAME);
        subEnvKeys.add(AGENT_SYSTEM_NAME);
        envKeys.add(AGENT_SERVER_ENABLED_KEY);
        envKeys.add(AGENT_SERVER_PORT_KEY);
    }

    static Map<String, String> updateEnvCfg() {
        Map<String, String> envCfg = new TreeMap<>();

        for (String key : subEnvKeys) {
            String value = System.getProperty(key);
            if (!StringUtils.isEmpty(value)) {
                envCfg.put(key.substring("easeagent.".length()), value);
            }
        }
        for (String key : envKeys) {
            String value = System.getProperty(key);
            if (!StringUtils.isEmpty(value)) {
                envCfg.put(key, value);
            }
        }

        String configEnv = SystemEnv.get(EASEAGENT_ENV_CONFIG);
        if (StringUtils.isNotEmpty(configEnv)) {
            Map<String, Object> map = JsonUtil.toMap(configEnv);
            Map<String, String> strMap = new HashMap<>();
            if (!map.isEmpty()) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    strMap.put(entry.getKey(), entry.getValue().toString());
                }
            }
            envCfg.putAll(strMap);
        }

        return envCfg;
    }

    private ConfigFactory() {
    }


    public static GlobalConfigs loadConfigs(String pathname, ClassLoader loader) {
        // load property configuration file if exist
        GlobalConfigs configs = loadDefaultConfigs(loader, CONFIG_PROP_FILE);

        // load yaml configuration file if exist
        GlobalConfigs yConfigs = loadDefaultConfigs(loader, CONFIG_YAML_FILE);
        configs.mergeConfigs(yConfigs);

        // override by user special config file
        if (StringUtils.isNotEmpty(pathname)) {
            GlobalConfigs configsFromOuterFile = ConfigLoader.loadFromFile(new File(pathname));
            LOGGER.info("Loaded user special config file: {}", pathname);
            configs.mergeConfigs(configsFromOuterFile);
        }

        // check environment cfg override
        configs.updateConfigsNotNotify(updateEnvCfg());

        if (LOGGER.isDebugEnabled()) {
            final String display = configs.toPrettyDisplay();
            LOGGER.debug("Loaded conf:\n{}", display);
        }
        return configs;
    }

    private static GlobalConfigs loadDefaultConfigs(ClassLoader loader, String file) {
        GlobalConfigs globalConfigs = JarFileConfigLoader.load(file);
        if (globalConfigs != null) {
            return globalConfigs;
        }
        return ConfigLoader.loadFromClasspath(loader, file);
    }

}
