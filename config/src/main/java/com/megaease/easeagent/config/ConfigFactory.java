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

import com.megaease.easeagent.config.yaml.YamlReader;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.utils.SystemEnv;
import com.megaease.easeagent.plugin.utils.common.JsonUtil;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import org.yaml.snakeyaml.parser.ParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    static Map<String, String> updateEnvCfg(Map<String, String> fileCfgMap) {
        for (String key : subEnvKeys) {
            String value = System.getProperty(key);
            if (!StringUtils.isEmpty(value)) {
                fileCfgMap.put(key.substring("easeagent.".length()), value);
            }
        }
        for (String key : envKeys) {
            String value = System.getProperty(key);
            if (!StringUtils.isEmpty(value)) {
                fileCfgMap.put(key, value);
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
            fileCfgMap.putAll(strMap);
        }

        return fileCfgMap;
    }

    private ConfigFactory() {
    }

    private static boolean checkYaml(String filename) {
        return filename.endsWith(".yaml") || filename.endsWith(".yml");
    }

    private static GlobalConfigs loadFromStream(InputStream in, String filename) throws IOException {
        if (in != null) {
            Map<String, String> map;
            if (checkYaml(filename)) {
                try {
                    map = new YamlReader().load(in).compress();
                } catch (ParserException e) {
                    LOGGER.warn("Wrong Yaml format, load config file failure: {}", filename);
                    map = Collections.emptyMap();
                }
            } else {
                map = extractPropsMap(in);
            }
            return new GlobalConfigs(map);
        } else {
            return new GlobalConfigs(Collections.emptyMap());
        }
    }

    private static GlobalConfigs loadFromClasspath(ClassLoader classLoader, String file) {
        try (InputStream in = classLoader.getResourceAsStream(file)) {
            return loadFromStream(in, file);
        } catch (IOException e) {
            LOGGER.warn("Load config file:{} by classloader:{} failure: {}", file, classLoader.toString(), e);
        }

        return new GlobalConfigs(Collections.emptyMap());
    }

    public static GlobalConfigs loadFromFile(File file) {
        try (FileInputStream in = new FileInputStream(file)) {
            return loadFromStream(in, file.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.warn("Load config file failure: {}", file.getAbsolutePath());
        }
        return new GlobalConfigs(Collections.emptyMap());
    }

    public static GlobalConfigs loadConfigs(String pathname, ClassLoader loader) {
        // load property configuration file if exist
        GlobalConfigs configs = ConfigFactory.loadFromClasspath(loader, CONFIG_PROP_FILE);

        // load yaml configuration file if exist
        GlobalConfigs yConfigs = ConfigFactory.loadFromClasspath(loader, CONFIG_YAML_FILE);
        configs.mergeConfigs(yConfigs);

        // override by user special config file
        if (StringUtils.isNotEmpty(pathname)) {
            GlobalConfigs configsFromOuterFile = ConfigFactory.loadFromFile(new File(pathname));
            configs.mergeConfigs(configsFromOuterFile);
        }

        // check environment cfg override
        configs.updateConfigsNotNotify(updateEnvCfg(configs.getConfigs()));

        if (LOGGER.isDebugEnabled()) {
            final String display = configs.toPrettyDisplay();
            LOGGER.debug("Loaded conf:\n{}", display);
        }
        return configs;
    }

    private static HashMap<String, String> extractPropsMap(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);
        HashMap<String, String> map = new HashMap<>();
        for (String one : properties.stringPropertyNames()) {
            map.put(one, properties.getProperty(one));
        }
        return map;
    }
}
