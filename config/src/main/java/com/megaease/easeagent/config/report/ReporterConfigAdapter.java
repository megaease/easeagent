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
import com.megaease.easeagent.plugin.utils.common.StringUtils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;
import static com.megaease.easeagent.config.ConfigUtils.*;

@Slf4j
public class ReporterConfigAdapter {
    private ReporterConfigAdapter() {}

    public static void convertReportConfig(Config config) {
        Map<String, String> cfg = extractReporterConfig(config);
        config.updateConfigsNotNotify(cfg);
    }

    public static Map<String, String> extractReporterConfig(Config config) {
        // outputServer config
        Map<String, String> extract = extractAndConvertPrefix(config, OUTPUT_SERVER_V1, OUTPUT_SERVER_V2);
        Map<String, String> cfg = new HashMap<>(extract);

        // encoder config
        final String[] encoder = {TRACE_ENCODER, METRIC_ENCODER};
        bindProp(TRACE_ENCODER, config, Config::getString, v -> encoder[0] = v, SPAN_JSON_ENCODER_NAME);
        bindProp(METRIC_ENCODER, config, Config::getString, v -> encoder[1] = v, METRIC_JSON_ENCODER_NAME);
        cfg.put(TRACE_ENCODER, encoder[0]);
        cfg.put(METRIC_ENCODER, encoder[1]);

        // async config
        extract = extractAndConvertPrefix(config, TRACE_OUTPUT_V1, TRACE_ASYNC);

        // trace output to v2 config
        String target = config.getString(TRACE_OUTPUT_TARGET_V1);
        if (StringUtils.isEmpty(target)) {
            cfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
        } else if ("system".equals(target)) {
            // check output servers
            if (StringUtils.hasText(cfg.get(BOOTSTRAP_SERVERS))) {
                cfg.put(TRACE_SENDER_NAME, KAFKA_SENDER_NAME);
                cfg.put(join(TRACE_SENDER, TOPIC_KEY), extract.remove(join(TRACE_ASYNC, TOPIC_KEY)));
            } else {
                cfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            }
        } else if ("zipkin".equals(target)) {
            cfg.put(TRACE_SENDER_NAME, ZIPKIN_SENDER_NAME);
            cfg.put(join(TRACE_SENDER, "zipkinUrl"), extract.remove(join(TRACE_ASYNC, "target.zipkinUrl")));
        } else {
            cfg.put(TRACE_SENDER_NAME, CONSOLE_SENDER_NAME);
            log.error("Unsupported output configuration item:{}={}", TRACE_OUTPUT_TARGET_V1, target);
        }
        extract.remove(join(TRACE_ASYNC, "target"));
        extract.remove(join(TRACE_ASYNC, "target.zipkinUrl"));
        cfg.putAll(extract);

        // v1 metric config
        extractGlobalMetricCfg(config, cfg);

        // v2 configuration migrate and override
        extract = extractByPrefix(config, REPORT);
        cfg.putAll(extract);

        return cfg;
    }

    private static void extractGlobalMetricCfg(Config config, Map<String, String> cfg) {
        Map<String, String> extract = extractAndConvertPrefix(config, GLOBAL_METRIC, METRIC_ASYNC);
        extract.remove(join(METRIC_ASYNC, "enabled"));
        String appendType = extract.remove(join(METRIC_ASYNC, "appendType"));
        if ("kafka".equals(appendType)) {
            appendType = METRIC_KAFKA_SENDER_NAME;
        }
        cfg.put(METRIC_SENDER_NAME, appendType);
        cfg.put(METRIC_SENDER_TOPIC, extract.remove(join(METRIC_ASYNC, "topic")));
        cfg.putAll(extract);
    }
}
