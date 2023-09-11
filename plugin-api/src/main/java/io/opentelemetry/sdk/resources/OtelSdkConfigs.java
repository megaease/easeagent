/*
 * Copyright (c) 2023, Inspireso and/or its affiliates. All rights reserved.
 */

package io.opentelemetry.sdk.resources;

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
    static final String OTEL_RESOURCE_ATTRIBUTES = "OTEL_RESOURCE_ATTRIBUTES";

    private static final Splitter.MapSplitter OTEL_RESOURCE_ATTRIBUTES_SPLITTER
        = Splitter.on(",")
        .omitEmptyStrings()
        .withKeyValueSeparator("=");

    private static final Map<String, String> SDK_ATTRIBUTES_TO_EASE_AGENT_PROPS =
        ImmutableMap.<String, String>builder()
            .put("javaagent.configuration-file", "config.path")
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
     * update config value from environment variables and java properties
     * <p>
     * java properties > environment variables > OTEL_RESOURCE_ATTRIBUTES
     */
    public static Map<String, String> updateEnvCfg() {
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
