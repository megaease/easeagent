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

import java.util.HashMap;
import java.util.Map;

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

    static MetricProps newDefault(Config reportConfig, String prefix) {
        return new Default(reportConfig, prefix);
    }

    class Default implements MetricProps {
        private volatile String senderName;
        private final boolean enabled;

        // for kafka sender
        private final String topic;
        private final String name;

        private int interval;
        private final Config config;
        private final String senderPrefix;
        private final Map<String, String> pluginConfigMap;

        public Default(Config reportConfig, IPluginConfig pluginConfig) {
            this.config = reportConfig;
            this.name = pluginConfig.namespace();
            this.enabled = pluginConfig.enabled();
            this.senderPrefix = generatePrefix();

            // low priority: global level
            Map<String, String> pCfg = ConfigUtils.extractByPrefix(reportConfig, REPORT);
            pCfg.putAll(ConfigUtils.extractAndConvertPrefix(pCfg, METRIC_SENDER, senderPrefix));

            // high priority: override by plugin level config
            pluginConfig.keySet().forEach(key -> {
                if (key.equals("appendType")) {
                    pCfg.put(join(senderPrefix, NAME_KEY), pluginConfig.getString(key));
                } else {
                    pCfg.put(join(senderPrefix, key), pluginConfig.getString(key));
                }
            });

            this.senderName = NoNull.of(pCfg.get(join(senderPrefix, NAME_KEY)), Const.METRIC_DEFAULT_APPEND_TYPE);
            this.topic = NoNull.of(pCfg.get(join(senderPrefix, TOPIC_KEY)), Const.METRIC_DEFAULT_TOPIC);
            if (pCfg.get(join(senderPrefix, "interval")) != null) {
                try {
                    this.interval = Integer.parseInt(pCfg.get(join(senderPrefix, "interval")));
                } catch (NumberFormatException e) {
                    this.interval = Const.METRIC_DEFAULT_INTERVAL;
                }
            } else {
                this.interval = Const.METRIC_DEFAULT_INTERVAL;
            }
            checkSenderName();
            pCfg.put(join(senderPrefix, "name"), this.senderName);
            pCfg.put(join(senderPrefix, "appenderName"), this.name);
            pCfg.put(join(senderPrefix, "output.interval"), Integer.toString(this.interval));

            this.pluginConfigMap = pCfg;
        }

        public Default(Config reportConfig, String prefix) {
            this.config = reportConfig;
            this.senderPrefix = prefix;
            this.name = this.config.getString(join(this.senderPrefix, "appenderName"));
            this.enabled = this.config.getBoolean(join(this.senderPrefix, ENABLED_KEY));
            this.senderName = this.config.getString(join(this.senderPrefix, NAME_KEY));
            this.topic = this.config.getString(join(this.senderPrefix, TOPIC_KEY));
            this.interval = this.config.getInt(join(this.senderPrefix, METRIC_ASYNC_INTERVAL));

            checkSenderName();
            this.pluginConfigMap = new HashMap<>(this.config.getConfigs());
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

        @Override
        public int hashCode() {
            return this.pluginConfigMap.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Default)) {
                return false;
            }
            Default other = (Default) o;
            return this.pluginConfigMap.equals(other.pluginConfigMap);
        }

        private String generatePrefix() {
            return "reporter.metric." + this.name + ".sender";
        }
    }
}
