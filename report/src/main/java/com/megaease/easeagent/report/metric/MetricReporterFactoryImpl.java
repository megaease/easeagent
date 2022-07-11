/*
 * Copyright (c) 2022, MegaEase
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

package com.megaease.easeagent.report.metric;

import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.ConfigChangeListener;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.report.ByteWrapper;
import com.megaease.easeagent.plugin.report.EncodedData;
import com.megaease.easeagent.plugin.report.metric.MetricReporterFactory;
import com.megaease.easeagent.report.plugin.ReporterRegistry;
import com.megaease.easeagent.report.sender.SenderWithEncoder;
import com.megaease.easeagent.report.util.Utils;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MetricReporterFactoryImpl implements MetricReporterFactory, ConfigChangeListener {
    private final ConcurrentHashMap<String, DefaultMetricReporter> reporters;
    private final Config reportConfig;

    public MetricReporterFactoryImpl(Config reportConfig) {
        this.reporters = new ConcurrentHashMap<>();
        this.reportConfig = reportConfig;
        this.reportConfig.addChangeListener(this);
    }

    public static MetricReporterFactory create(Config reportConfig) {
        return new MetricReporterFactoryImpl(reportConfig);
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
        this.reportConfig.updateConfigs(changes);
    }

    private Map<String, String> filterChanges(List<ChangeItem> list) {
        Map<String, String> cfg = new HashMap<>();
        list.forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));

        return cfg;
    }

    public static class DefaultMetricReporter implements Reporter, ConfigChangeListener {
        private MetricProps metricProps;
        private SenderWithEncoder sender;
        private final IPluginConfig pluginConfig;
        private final Config reportConfig;
        private final Config metricConfig;

        public DefaultMetricReporter(IPluginConfig pluginConfig, Config reportConfig) {
            this.pluginConfig = pluginConfig;

            this.reportConfig = reportConfig;
            this.reportConfig.addChangeListener(this);

            this.metricProps = Utils.extractMetricProps(this.pluginConfig, reportConfig);
            this.metricConfig = this.metricProps.asReportConfig();

            this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.metricConfig);
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

        public Config getMetricConfig() {
            return this.metricConfig;
        }

        public MetricProps getMetricProps() {
            return this.metricProps;
        }

        public SenderWithEncoder getSender() {
            return this.sender;
        }

        @Override
        public void onChange(List<ChangeItem> list) {
            if (list.isEmpty()) {
                return;
            }

            String senderName = this.metricProps.getSenderName();
            this.metricProps = Utils.extractMetricProps(pluginConfig, this.reportConfig);
            Config changedConfig = this.metricProps.asReportConfig();

            this.metricConfig.updateConfigs(changedConfig.getConfigs());

            if (!metricProps.getSenderName().equals(senderName)) {
                this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.metricConfig);
            }
        }
    }
}
