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

package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.config.ConfigUtils;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.Const;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.sender.AgentLoggerSender;
import com.megaease.easeagent.report.sender.metric.MetricKafkaSender;
import com.megaease.easeagent.report.util.Utils;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.*;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public interface MetricProps {
    String getName();

    String getSenderPrefix();

    String getSenderName();

    String getTopic();

    int getInterval();

    boolean isEnabled();

    Map<String, String> toReportConfigMap();

    Configs asReportConfig();

    static MetricProps newDefault(IPluginConfig config, Config reportConfig) {
        return new Default(reportConfig, config);
    }

    static MetricProps newDefault(Config reportConfig) {
        return new Default(reportConfig,
                reportConfig.getString(METRIC_SENDER_APPENDER),
                reportConfig.getBoolean(METRIC_SENDER_ENABLED),
                NoNull.of(reportConfig.getString(METRIC_SENDER_NAME), Const.METRIC_DEFAULT_APPEND_TYPE),
                NoNull.of(reportConfig.getString(METRIC_SENDER_TOPIC), Const.METRIC_DEFAULT_TOPIC),
                NoNull.of(reportConfig.getInt(METRIC_ASYNC_INTERVAL),
                    Const.METRIC_DEFAULT_INTERVAL));
    }

    class Default implements MetricProps {
        private volatile String senderName;
        private final boolean enabled;
        private final String topic;
        private final String name;
        private final int interval;
        private final Config config;
        private final String senderPrefix;
        private final Map<String, String> pluginConfigMap;

        public Default(Config reportConfig, IPluginConfig pluginConfig) {
            this.config = reportConfig;
            this.name = pluginConfig.namespace();
            this.enabled = pluginConfig.enabled();
            this.senderPrefix = generatePrefix();

            this.senderName = NoNull.of(pluginConfig.getString(KEY_COMM_APPEND_TYPE),
                NoNull.of(reportConfig.getString(METRIC_SENDER_NAME), Const.METRIC_DEFAULT_APPEND_TYPE));
            this.topic = NoNull.of(pluginConfig.getString(KEY_COMM_TOPIC),
                NoNull.of(reportConfig.getString(METRIC_SENDER_TOPIC), Const.METRIC_DEFAULT_TOPIC));
            this.interval = NoNull.of(pluginConfig.getInt(KEY_COMM_INTERVAL),
                NoNull.of(reportConfig.getInt(METRIC_ASYNC_INTERVAL), Const.METRIC_DEFAULT_INTERVAL));
            checkSenderName();

            HashMap<String, String> pluginCfgMap = new HashMap<>();
            // global level
            Map<String, String> pCfg = ConfigUtils.extractByPrefix(reportConfig, METRIC_V2);
            pCfg = ConfigUtils.extractAndConvertPrefix(pCfg, METRIC_SENDER, senderPrefix);
            pluginCfgMap.putAll(pCfg);
            // plugin level
            pluginConfig.keySet().forEach(key -> pluginCfgMap.put(key, pluginConfig.getString(key)));
            pCfg = ConfigUtils.extractAndConvertPrefix(pluginCfgMap,
                "plugin." + pluginConfig.domain() + "." + pluginConfig.namespace() + ".metric",
                this.senderPrefix);
            pCfg.put(join(senderPrefix, "name"), this.senderName);
            this.pluginConfigMap = pCfg;

            // remove later
            this.pluginConfigMap.put(METRIC_SENDER_APPENDER, this.name);
            this.pluginConfigMap.put(METRIC_SENDER_NAME, this.senderName);
            this.pluginConfigMap.put(METRIC_SENDER_TOPIC, this.topic);
            this.pluginConfigMap.put(METRIC_ASYNC_INTERVAL, String.valueOf(this.interval));
        }

        public Default(Config reportConfig, String name, boolean enabled,
                       String senderName, String topic, int interval) {
            this.name = name;
            this.enabled = enabled;
            this.senderName = senderName;
            this.topic = topic;
            this.interval = interval;
            this.config = reportConfig;
            this.senderPrefix = generatePrefix();
            checkSenderName();
            this.pluginConfigMap = new HashMap<>();

            // remove later
            this.pluginConfigMap.put(METRIC_SENDER_APPENDER, this.name);
            this.pluginConfigMap.put(METRIC_SENDER_NAME, this.senderName);
            this.pluginConfigMap.put(METRIC_SENDER_TOPIC, this.topic);
            this.pluginConfigMap.put(METRIC_ASYNC_INTERVAL, String.valueOf(this.interval));
        }

        private void checkSenderName() {
            if ("kafka".equals(this.senderName)) {
                this.senderName = MetricKafkaSender.SENDER_NAME;
            }

            String bootstrapServers = this.config.getString(BOOTSTRAP_SERVERS);
            if (StringUtils.isEmpty(bootstrapServers) && this.senderName.equals(MetricKafkaSender.SENDER_NAME)) {
                this.senderName = AgentLoggerSender.SENDER_NAME;
            }
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getSenderPrefix() {
            return this.senderPrefix;
        }

        @Override
        public String getSenderName() {
            return this.senderName;
        }

        @Override
        public String getTopic() {
            return this.topic;
        }

        @Override
        public boolean isEnabled() {
            return this.enabled;
        }

        @Override
        public Map<String, String> toReportConfigMap() {
            return this.pluginConfigMap;
        }

        public Configs asReportConfig() {
            // merge plugin and global config
            Map<String, String> cfg = this.config.getConfigs();
            cfg.putAll(toReportConfigMap());
            return new Configs(cfg);
        }

        @Override
        public int getInterval() {
            return this.interval;
        }

        private String generatePrefix() {
            return "reporter.metric." + this.name + ".sender";
        }
    }
}
