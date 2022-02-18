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
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.ReportConfigChange;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import com.megaease.easeagent.report.sender.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.megaease.easeagent.config.report.ReportConfigConst.*;

public class MetricReporterImpl implements MetricReporter {
    private final ConcurrentHashMap<String, DefaultMetricReporter> reporters;
    private final AppenderManager appenderManager;
    private final OutputProperties outputProperties;
    private final Config reportConfig;

    public MetricReporterImpl(Config configs) {
        this.reporters = new ConcurrentHashMap<>();
        outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.reportConfig = configs;
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
            reporter = new DefaultMetricReporter(pluginConfig, this.reportConfig);
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
        if (Utils.isOutputPropertiesChange(changes)) {
            this.outputProperties.updateConfig(changes);
            appenderManager.refresh();
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

        public DefaultMetricReporter(IPluginConfig pluginConfig, Config reportConfig) {
            this.pluginConfig = pluginConfig;
            pluginConfig.addChangeListener(this);

            this.metricProps = Utils.extractMetricProps(pluginConfig, reportConfig);

            this.reporterConfig = this.metricProps.asReportConfig();

            this.sender = ReporterRegistry.getSender(METRIC_SENDER, this.reporterConfig);
        }

        public void report(String context) {
            try {
                sender.send(new ByteWrapper(context.getBytes())).execute();
            } catch (IOException e) {
                // ignored
            }
        }

        @Override
        public void report(EncodedData encodedData) {
            try {
                sender.send(encodedData).execute();
            } catch (IOException e) {
                // ignored
            }
        }

        @Override
        public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
            MetricProps newProps = Utils.extractMetricProps(newConfig, reportConfig);

            this.pluginConfig = newConfig;
            if (metricProps.getTopic().equals(newProps.getTopic())
                && metricProps.getSenderName().equals(newProps.getSenderName())) {
                return;
            }

            this.metricProps = newProps;
            this.reporterConfig.updateConfigs(metricProps.toReportConfigMap());
            this.sender = ReporterRegistry.getSender(METRIC_SENDER_NAME, reportConfig);
        }

        @Override
        public void updateConfigs(Map<String, String> changes) {
            MetricProps newProps = Utils.extractMetricProps(pluginConfig, reportConfig);

            if (metricProps.getTopic().equals(newProps.getTopic())
                && metricProps.getSenderName().equals(newProps.getSenderName())) {
                return;
            }

            this.metricProps = newProps;

            changes.putAll(metricProps.toReportConfigMap());
            this.reporterConfig.updateConfigs(changes);
            this.sender = ReporterRegistry.getSender(METRIC_SENDER_NAME, reportConfig);
        }
    }
}
