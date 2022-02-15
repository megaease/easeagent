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
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

import static com.megaease.easeagent.config.ValidateUtils.*;

public class ConfigFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);
    private static final String CONFIG_FILE = "agent.properties";

    private ConfigFactory() {}

    public static GlobalConfigs loadFromClasspath(ClassLoader classLoader) {
        try {
            InputStream inputStream = classLoader.getResourceAsStream(CONFIG_FILE);
            if (inputStream != null) {
                final HashMap<String, String> propsMap = extractPropsMap(inputStream);
                return new GlobalConfigs(propsMap);
            }
        } catch (IOException e) {
            LOGGER.warn("Load config file:{} by classloader:{} failure: {}", CONFIG_FILE, classLoader.toString(), e);
        }
        return new GlobalConfigs(Collections.emptyMap());
    }

    public static Configs loadFromFile(File file) {
        try {
            try (FileInputStream in = new FileInputStream(file)) {
                HashMap<String, String> map = extractPropsMap(in);
                return new GlobalConfigs(map);
            }
        } catch (IOException e) {
            LOGGER.warn("Load config file failure: {}", file.getAbsolutePath());
        }
        return new GlobalConfigs(Collections.emptyMap());
    }

    public static GlobalConfigs loadConfigs(String pathname, ClassLoader loader) {
        GlobalConfigs configs = ConfigFactory.loadFromClasspath(loader);
        if (StringUtils.isNotEmpty(pathname)) {
            Configs configsFromOuterFile = ConfigFactory.loadFromFile(new File(pathname));
            configs.updateConfigsNotNotify(configsFromOuterFile.getConfigs());
        }

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

    public static void validConfigs(Configs configs) {
        //validate serviceName and systemName
        ValidateUtils.validate(configs, ConfigConst.SERVICE_NAME, HasText);
        ValidateUtils.validate(configs, ConfigConst.SYSTEM_NAME, HasText);
        //validate output
        ValidateUtils.validate(configs, ConfigConst.Observability.OUTPUT_ENABLED, HasText, Bool);
        ValidateUtils.validate(configs, ConfigConst.Observability.OUTPUT_SERVERS, HasText);
        ValidateUtils.validate(configs, ConfigConst.Observability.OUTPUT_TIMEOUT, HasText, NumberInt);
        //validate metrics
        ValidateUtils.validate(configs, ConfigConst.Observability.METRICS_ENABLED, HasText, Bool);
        //validate trace
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_ENABLED, HasText, Bool);
        //validate trace output
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_ENABLED, HasText, Bool);
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_MESSAGE_MAX_BYTES, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_MESSAGE_TIMEOUT, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_QUEUED_MAX_SIZE, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_QUEUED_MAX_SPANS, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_REPORT_THREAD, HasText, NumberInt);
        ValidateUtils.validate(configs, ConfigConst.Observability.TRACE_OUTPUT_TOPIC, HasText);
    }
}
