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
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.Const;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import static com.megaease.easeagent.config.ConfigUtils.extractAndConvertPrefix;
import static com.megaease.easeagent.config.ConfigUtils.extractByPrefix;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;

@Slf4j
public class ReportConfigAdapter {
    private ReportConfigAdapter() {}

    public static void convertConfig(Map<String, String> config) {
        Map<String, String> cfg = extractAndConvertReporterConfig(config);
        config.putAll(cfg);
    }

    public static Map<String, String> extractReporterConfig(Config configs) {
        Map<String, String> cfg = extractAndConvertReporterConfig(configs.getConfigs());

        // default config
        cfg.put(TRACE_ENCODER, NoNull.of(cfg.get(TRACE_ENCODER), SPAN_JSON_ENCODER_NAME));
        cfg.put(METRIC_ENCODER, NoNull.of(cfg.get(METRIC_ENCODER), METRIC_JSON_ENCODER_NAME));
        cfg.put(LOG_ENCODER, NoNull.of(cfg.get(LOG_ENCODER), LOG_JSON_ENCODER_NAME));

        cfg.put(TRACE_SENDER_NAME, NoNull.of(cfg.get(TRACE_SENDER_NAME), getDefaultAppender(cfg)));
        cfg.put(METRIC_SENDER_NAME, NoNull.of(cfg.get(METRIC_SENDER_NAME), getDefaultAppender(cfg)));
        cfg.put(LOG_SENDER_NAME, NoNull.of(cfg.get(LOG_SENDER_NAME), getDefaultAppender(cfg)));

        return cfg;
    }

    public static String getDefaultAppender(Map<String, String> cfg) {
        String outputAppender = cfg.get(join(OUTPUT_SERVER_V2, APPEND_TYPE_KEY));

        if (StringUtils.isEmpty(outputAppender)) {
            return Const.DEFAULT_APPEND_TYPE;
        }

        return outputAppender;
    }

    private static Map<String, String> extractAndConvertReporterConfig(Map<String, String> srcConfig) {
        Map<String, String> extract = extractTracingConfig(srcConfig);
        Map<String, String> outputCfg = new TreeMap<>(extract);

        // metric config
        extract = extractMetricPluginConfig(srcConfig);
        outputCfg.putAll(extract);

        // access metric access log
        updateAccessLogCfg(outputCfg);

        // all extract configuration will be override by config items start with "report" in srcConfig
        extract = extractByPrefix(srcConfig, REPORT);
        outputCfg.putAll(extract);

        return outputCfg;
    }


    /**
     * this can be deleted if there is not any v1 configuration needed to compatible with
     * convert v1 tracing config to v2
     */
    private static Map<String, String> extractTracingConfig(Map<String, String> srcCfg) {
        // outputServer config
        Map<String, String> extract = extractAndConvertPrefix(srcCfg, OUTPUT_SERVER_V1, OUTPUT_SERVER_V2);
        Map<String, String> outputCfg = new TreeMap<>(extract);

        // async output config
        extract = extractAndConvertPrefix(srcCfg, TRACE_OUTPUT_V1, TRACE_ASYNC);

        String target = srcCfg.get(join(TRACE_OUTPUT_V1, "target"));
        extract.remove(join(TRACE_ASYNC, "target"));

        if (!StringUtils.isEmpty(outputCfg.get(TRACE_SENDER_NAME))) {
            log.info("Reporter V2 config trace sender as: {}", outputCfg.get(TRACE_SENDER_NAME));
        } else if ("system".equals(target)) {
            // check output servers
            if (StringUtils.hasText(outputCfg.get(BOOTSTRAP_SERVERS))) {
                outputCfg.put(TRACE_SENDER_NAME, KAFKA_SENDER_NAME);
                outputCfg.put(TRACE_SENDER_TOPIC_V2, extract.remove(join(TRACE_ASYNC, TOPIC_KEY)));
            } else {
                outputCfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            }
        } else if ("zipkin".equals(target)) {
            outputCfg.put(TRACE_SENDER_NAME, ZIPKIN_SENDER_NAME);
            String url = extract.remove(join(TRACE_ASYNC, "target.zipkinUrl"));
            if (StringUtils.isEmpty(url)) {
                outputCfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            } else {
                outputCfg.put(join(TRACE_SENDER, "url"), url);
            }
        } else if (!StringUtils.isEmpty(target)) {
            outputCfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            log.info("Unsupported output configuration item:{}={}", TRACE_OUTPUT_TARGET_V1, target);
        }
        outputCfg.putAll(extract);

        return outputCfg;
    }

    /**
     * call after metric config adapter
     *
     * extract 'reporter.metric.access.*' to 'reporter.log.*'
     */
    private static void updateAccessLogCfg(Map<String, String> outputCfg) {
        // reporter.metric.access.*
        String prefix = join(METRIC_V2, ConfigConst.Namespace.ACCESS);
        Map<String, String> accessLog = extractAndConvertPrefix(outputCfg, prefix, LOGS);

        outputCfg.putAll(accessLog);
    }

    /**
     * metric report configuration
     *
     * extract `plugin.observability.global.metric.*` config items to reporter.metric.sender.*`
     *
     * extract `plugin.observability.[namespace].metric.*` config items
     * to reporter.metric.[namespace].sender.*`
     *
     * @param srcCfg source configuration map
     * @return metric reporter config start with 'reporter.metric.[namespace].sender'
     */
    private static Map<String, String> extractMetricPluginConfig(Map<String, String> srcCfg) {
        final String globalKey = "." + ConfigConst.PLUGIN_GLOBAL + ".";
        final String prefix = join(ConfigConst.PLUGIN, ConfigConst.OBSERVABILITY);
        Map<String, String> metricConfigs = new HashMap<>();
        int metricKeyLength = ConfigConst.METRIC_SERVICE_ID.length();

        for (Map.Entry<String, String> e : srcCfg.entrySet()) {
            String key = e.getKey();
            if (!key.startsWith(prefix)) {
                continue;
            }
            int idx = key.indexOf(ConfigConst.METRIC_SERVICE_ID, prefix.length());
            if (idx < 0) {
                continue;
            }
            String namespace = key.substring(prefix.length(), idx);
            String suffix = key.substring(idx + metricKeyLength + 1);
            String newKey;

            if (namespace.equals(globalKey)) {
                namespace = ".";
            }

            if (suffix.equals(ENCODER_KEY)) {
                newKey = METRIC_V2 + namespace + ENCODER_KEY;
            } else if (suffix.equals(INTERVAL_KEY)) {
                newKey = METRIC_V2 + namespace + join(ASYNC_KEY, suffix);
            } else {
                newKey = METRIC_V2 + namespace + join(SENDER_KEY, suffix);
            }

            if (newKey.endsWith(APPEND_TYPE_KEY) && e.getValue().equals("kafka")) {
                metricConfigs.put(newKey, METRIC_KAFKA_SENDER_NAME);
            } else {
                metricConfigs.put(newKey, e.getValue());
            }


        }

        return metricConfigs;
    }
}
