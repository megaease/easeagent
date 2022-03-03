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
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import com.megaease.easeagent.plugin.report.ByteWrapper;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.report.ReportConfigChange;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import com.megaease.easeagent.report.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.megaease.easeagent.config.report.ReportConfigConst.METRIC_V2;
import static com.megaease.easeagent.config.report.ReportConfigConst.OUTPUT_SERVER_V2;

@Slf4j
public class MetricReporterImpl implements MetricReporter {
    private final ConcurrentHashMap<String, DefaultMetricReporter> reporters;
    private final Config metricConfig;

    public MetricReporterImpl(Config configs) {
        this.reporters = new ConcurrentHashMap<>();
        this.metricConfig = configs;
        configs.addChangeListener(this);
    }

    public static MetricReporter create(Config config) {
        return new MetricReporterImpl(config);
    }

    @Override
    public Reporter reporter(IPluginConfig pluginConfig) {
        DefaultMetricReporter reporter = reporters.get(pluginConfig.namespace());
        if (reporter != null) {
            return reporter;
        }
        synchronized (reporters) {
            reporter = reporters.get(pluginConfig.namespace());
            if (reporter != null) {
                return reporter;
            }
            reporter = new DefaultMetricReporter(pluginConfig, this.metricConfig);
            reporters.put(pluginConfig.namespace(), reporter);
            return reporter;
        }
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        Map<String, String> changes = filterChanges(list);
        if (changes.isEmpty()) {
            return;
        }

        reporters.forEachValue(1, reporter -> reporter.updateConfigs(changes));
    }

    private Map<String, String> filterChanges(List<ChangeItem> list) {
        Map<String, String> cfg = new HashMap<>();
        list.stream()
            .filter(one -> {
                String name = one.getFullName();
                return name.startsWith(OUTPUT_SERVER_V2)
                    || name.startsWith(METRIC_V2);
            }).forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));

        return cfg;
    }

    public class DefaultMetricReporter implements Reporter, ReportConfigChange, PluginConfigChangeListener {
        private MetricProps metricProps;
        private SenderWithEncoder sender;
        private IPluginConfig pluginConfig;
        private final Config reporterConfig;

        public DefaultMetricReporter(IPluginConfig pluginConfig, Config config) {
            this.pluginConfig = pluginConfig;
            pluginConfig.addChangeListener(this);

            this.metricProps = Utils.extractMetricProps(pluginConfig, config);
            this.reporterConfig = this.metricProps.asReportConfig();

            this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.reporterConfig);
        }

        public void report(String context) {
            try {
                sender.send(new ByteWrapper(context.getBytes())).execute();
            } catch (IOException e) {
                log.warn("send error. {}", e.getMessage());
            }
        }

        @Override
        public void report(EncodedData encodedData) {
            try {
                sender.send(encodedData).execute();
            } catch (IOException e) {
                log.warn("send error. {}", e.getMessage());
            }
        }

        @Override
        public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
            MetricProps newProps = Utils.extractMetricProps(newConfig, metricConfig);

            String senderName = this.metricProps.getSenderName();
            this.pluginConfig = newConfig;
            this.metricProps = newProps;

            this.reporterConfig.updateConfigs(metricProps.asReportConfig().getConfigs());

            if (!metricProps.getSenderName().equals(senderName)) {
                this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.reporterConfig);
            }
        }

        @Override
        public void updateConfigs(Map<String, String> changes) {
            Map<String, String> nCfg = this.reporterConfig.getConfigs();
            nCfg.putAll(changes);

            String senderName = this.metricProps.getSenderName();
            this.metricProps = Utils.extractMetricProps(pluginConfig, new Configs(nCfg));

            this.reporterConfig.updateConfigs(this.metricProps.asReportConfig().getConfigs());

            if (!metricProps.getSenderName().equals(senderName)) {
                this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.reporterConfig);
            }
        }

        public Config getReporterConfig() {
            return this.reporterConfig;
        }

        public MetricProps getMetricProps() {
            return this.metricProps;
        }

        public SenderWithEncoder getSender() {
            return this.sender;
        }
    }
}
