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

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.Const;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.utils.NoNull;
import com.megaease.easeagent.plugin.utils.common.StringUtils;
import com.megaease.easeagent.report.sender.AgentLoggerSender;
import com.megaease.easeagent.report.sender.metric.MetricKafkaSender;

import java.util.HashMap;
import java.util.Map;

import static com.megaease.easeagent.plugin.api.config.ConfigConst.Observability.*;
import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public interface MetricProps {
    String getName();

    String getSenderName();

    String getTopic();

    int getInterval();

    boolean isEnabled();

    Map<String, String> toReportConfigMap();

    Configs asReportConfig();

    static MetricProps newDefault(IPluginConfig config, Config reportConfig) {

        return new Default(
            reportConfig,
            config.namespace(),
            config.enabled(),
            NoNull.of(config.getString(KEY_COMM_APPEND_TYPE),
                NoNull.of(reportConfig.getString(METRIC_SENDER_NAME), Const.METRIC_DEFAULT_APPEND_TYPE)),
            NoNull.of(config.getString(KEY_COMM_TOPIC),
                NoNull.of(reportConfig.getString(METRIC_SENDER_TOPIC), Const.METRIC_DEFAULT_TOPIC)),
            NoNull.of(config.getInt(KEY_COMM_INTERVAL),
                NoNull.of(reportConfig.getInt(METRIC_ASYNC_INTERVAL), Const.METRIC_DEFAULT_INTERVAL))
        );
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

        public Default(Config reportConfig, String name, boolean enabled,
                       String senderName, String topic, int interval) {
            this.name = name;
            this.enabled = enabled;
            this.senderName = senderName;
            this.topic = topic;
            this.interval = interval;
            this.config = reportConfig;

            if ("kafka".equals(this.senderName)) {
                this.senderName = MetricKafkaSender.SENDER_NAME;
            }

            String bootstrapServers = reportConfig.getString(BOOTSTRAP_SERVERS);
            if (StringUtils.isEmpty(bootstrapServers) && this.senderName.equals(MetricKafkaSender.SENDER_NAME)) {
                this.senderName = AgentLoggerSender.SENDER_NAME;
            }
        }

        @Override
        public String getName() {
            return this.name;
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
            Map<String, String> map = new HashMap<>();
            map.put(METRIC_SENDER_APPENDER, this.name);
            map.put(METRIC_SENDER_NAME, this.senderName);
            map.put(METRIC_SENDER_TOPIC, this.topic);
            map.put(METRIC_ASYNC_INTERVAL, String.valueOf(this.interval));
            return map;
        }

        public Configs asReportConfig() {
            Map<String, String> map = toReportConfigMap();
            // merge plugin and global config
            Map<String, String> cfg = this.config.getConfigs();
            cfg.putAll(toReportConfigMap());
            return new Configs(cfg);
        }

        @Override
        public int getInterval() {
            return this.interval;
        }
    }
}
