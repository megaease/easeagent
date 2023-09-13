/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.config;

import com.google.common.base.CaseFormat;
import com.google.common.base.Splitter;
import com.megaease.easeagent.plugin.utils.ImmutableMap;
import com.megaease.easeagent.plugin.utils.SystemEnv;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Compatible with opentelemetry-java.
 * <p>
 * {@see https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#disabling-opentelemetrysdk}
 */
public class OtelSdkConfigs {
    private static final String OTEL_RESOURCE_ATTRIBUTES = "OTEL_RESOURCE_ATTRIBUTES";

    private static final String CONFIG_PATH_PROP_KEY = "otel.javaagent.configuration-file";
    private static final String CONFIG_PATH_ENV_KEY = "OTEL_JAVAAGENT_CONFIGURATION_FILE";

    private static final Splitter.MapSplitter OTEL_RESOURCE_ATTRIBUTES_SPLITTER
        = Splitter.on(",")
        .omitEmptyStrings()
        .withKeyValueSeparator("=");

    private static final Map<String, String> SDK_ATTRIBUTES_TO_EASE_AGENT_PROPS =
        ImmutableMap.<String, String>builder()
            .put("sdk.disabled", "easeagent.server.enabled")
            .put("service.name", "name") //"easeagent.name"
            .put("service.namespace", "system") //"easeagent.system"
            .build();

    // -Dotel.service.name=xxx
    private static final Map<String, String> OTEL_SDK_PROPS_TO_EASE_AGENT_PROPS = new HashMap<>();

    // OTEL_SERVICE_NAME=xxx
    private static final Map<String, String> OTEL_SDK_ENV_VAR_TO_EASE_AGENT_PROPS = new HashMap<>();

    static {
        for (Map.Entry<String, String> entry : SDK_ATTRIBUTES_TO_EASE_AGENT_PROPS.entrySet()) {
            // lower.hyphen -> UPPER_UNDERSCORE
            OTEL_SDK_PROPS_TO_EASE_AGENT_PROPS.put(
                "otel." + entry.getKey(),
                entry.getValue()
            );
        }

        for (Map.Entry<String, String> entry : OTEL_SDK_PROPS_TO_EASE_AGENT_PROPS.entrySet()) {
            // lower.hyphen -> UPPER_UNDERSCORE
            OTEL_SDK_ENV_VAR_TO_EASE_AGENT_PROPS.put(
                CaseFormat.LOWER_HYPHEN.to(CaseFormat.UPPER_UNDERSCORE, entry.getKey().replace('.', '-')),
                entry.getValue()
            );
        }
    }

    /**
     * Get config path from java properties or environment variables
     */
    static String getConfigPath() {
        String path = System.getProperty(CONFIG_PATH_PROP_KEY);
        if (StringUtils.isEmpty(path)) {
            path = SystemEnv.get(CONFIG_PATH_ENV_KEY);
        }

        return path;
    }


    /**
     * update config value from environment variables and java properties
     * <p>
     * java properties > environment variables > OTEL_RESOURCE_ATTRIBUTES
     */
    static Map<String, String> updateEnvCfg() {
        Map<String, String> envCfg = new TreeMap<>();

        String configEnv = SystemEnv.get(OTEL_RESOURCE_ATTRIBUTES);
        if (StringUtils.isNotEmpty(configEnv)) {
            Map<String, String> map = OTEL_RESOURCE_ATTRIBUTES_SPLITTER.split(configEnv);
            if (!map.isEmpty()) {
                for (Map.Entry<String, String> entry : SDK_ATTRIBUTES_TO_EASE_AGENT_PROPS.entrySet()) {
                    String value = map.get(entry.getKey());
                    if (!StringUtils.isEmpty(value)) {
                        envCfg.put(entry.getValue(), value);
                    }
                }
            }
        }

        // override by environment variables, eg: export OTEL_SERVICE_NAME=xxx
        for (Map.Entry<String, String> entry : OTEL_SDK_ENV_VAR_TO_EASE_AGENT_PROPS.entrySet()) {
            String value = SystemEnv.get(entry.getKey());
            if (!StringUtils.isEmpty(value)) {
                envCfg.put(entry.getValue(), value);
            }
        }

        // override by java properties; eg: java -Dotel.service.name=xxx
        for (Map.Entry<String, String> entry : OTEL_SDK_PROPS_TO_EASE_AGENT_PROPS.entrySet()) {
            String value = System.getProperty(entry.getKey());
            if (!StringUtils.isEmpty(value)) {
                envCfg.put(entry.getValue(), value);
            }
        }

        return envCfg;
    }
}
