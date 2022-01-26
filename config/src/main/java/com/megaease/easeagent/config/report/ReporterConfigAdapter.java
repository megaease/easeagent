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
package com.megaease.easeagent.config.report;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;
import static com.megaease.easeagent.config.ConfigUtils.*;

@Slf4j
public class ReporterConfigAdapter {
    private ReporterConfigAdapter() {}

    public static void convertConfig(Map<String, String> config) {
        Map<String, String> cfg = extractAndConvertReporterConfig(config);
        config.putAll(cfg);
    }

    public static Map<String, String> extractReporterConfig(Config configs) {
        Map<String, String> cfg = extractAndConvertReporterConfig(configs.getConfigs());

        // default config
        cfg.put(TRACE_ENCODER, NoNull.of(cfg.get(TRACE_ENCODER), SPAN_JSON_ENCODER_NAME));
        cfg.put(METRIC_ENCODER, NoNull.of(cfg.get(METRIC_ENCODER), METRIC_JSON_ENCODER_NAME));
        cfg.put(TRACE_SENDER_NAME, NoNull.of(cfg.get(TRACE_SENDER_NAME), CONSOLE_SENDER_NAME));
        cfg.put(METRIC_SENDER_NAME, NoNull.of(cfg.get(METRIC_SENDER_NAME), CONSOLE_SENDER_NAME));

        return cfg;
    }

    public static Map<String, String> extractAndConvertReporterConfig(Map<String, String> config) {
        // outputServer config
        Map<String, String> extract = extractAndConvertPrefix(config, OUTPUT_SERVER_V1, OUTPUT_SERVER_V2);
        Map<String, String> cfg = new HashMap<>(extract);

        // async config
        extract = extractAndConvertPrefix(config, TRACE_OUTPUT_V1, TRACE_ASYNC);

        // trace output to v2 config
        String target = remove(join(TRACE_ASYNC, "target"), extract, config);
        if (!StringUtils.isEmpty(cfg.get(TRACE_SENDER_NAME))) {
            log.info("Reporter V2 config trace sender as: {}", cfg.get(TRACE_SENDER_NAME));
        } else if (StringUtils.isEmpty(target)) {
            log.info("Trace sender name is not found.");
        } else if ("system".equals(target)) {
            // check output servers
            if (StringUtils.hasText(cfg.get(BOOTSTRAP_SERVERS))) {
                cfg.put(TRACE_SENDER_NAME, KAFKA_SENDER_NAME);
                cfg.put(TRACE_SENDER_TOPIC_V2, remove(join(TRACE_ASYNC, TOPIC_KEY), extract, config));
            } else {
                cfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            }
        } else if ("zipkin".equals(target)) {
            cfg.put(TRACE_SENDER_NAME, ZIPKIN_SENDER_NAME);
            String url = remove(join(TRACE_ASYNC, "target.zipkinUrl"), extract, config);
            cfg.put(join(TRACE_SENDER, "zipkinUrl"), url);

            // wait for migrate
            cfg.put(TRACE_OUTPUT_TARGET_V1, target);
            cfg.put(TRACE_OUTPUT_TARGET_ZIPKIN_URL, url);
        } else {
            cfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            log.info("Unsupported output configuration item:{}={}", TRACE_OUTPUT_TARGET_V1, target);
        }
        remove(join(TRACE_ASYNC, "target.zipkinUrl"), extract, config);
        cfg.putAll(extract);

        // v1 metric config
        extractGlobalMetricCfg(config, cfg);

        // v2 configuration migrate and override
        extract = extractByPrefix(config, REPORT);
        cfg.putAll(extract);

        return cfg;
    }

    private static void extractGlobalMetricCfg(Map<String, String> config, Map<String, String> cfg) {
        Map<String, String> globalMetric = extractByPrefix(config, GLOBAL_METRIC);
        Map<String, String> extract = extractAndConvertPrefix(globalMetric, GLOBAL_METRIC, METRIC_ASYNC);

        remove(join(METRIC_ASYNC, ENABLED_KEY), extract, config);

        String appendType = remove(join(METRIC_ASYNC, "appendType"), extract, config);
        if ("kafka".equals(appendType)) {
            appendType = METRIC_KAFKA_SENDER_NAME;
        }

        if (!StringUtils.isEmpty(appendType)) {
            cfg.put(METRIC_SENDER_NAME, NoNull.of(appendType, CONSOLE_SENDER_NAME));
        }
        String topic = remove(join(METRIC_ASYNC, TOPIC_KEY), extract, config);
        if (!StringUtils.isEmpty(topic)) {
            cfg.put(METRIC_SENDER_TOPIC, topic);
        }
        cfg.putAll(extract);
    }

    private static String remove(String key, Map<String, String> extract, Map<String, String> config) {
        String v = extract.remove(key);
        config.remove(key);
        return v;
    }
}
