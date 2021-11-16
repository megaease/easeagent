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

import com.megaease.easeagent.config.ChangeItem;
import com.megaease.easeagent.config.ConfigChangeListener;
import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.PluginMetricReporter;
import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.util.List;

public class PluginMetricReporterImpl implements PluginMetricReporter {
    private final Configs configs;
    private final AppenderManager appenderManager;

    public PluginMetricReporterImpl(Configs configs) {
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(new PluginMetricReporterImpl.InternalListener());
    }

    public static PluginMetricReporter create(Configs config) {
        return new PluginMetricReporterImpl(config);
    }

    @Override
    public com.megaease.easeagent.plugin.api.Reporter reporter(Config config) {
        return new Reporter(config);
    }

    private class InternalListener implements ConfigChangeListener {
        @Override
        public void onChange(List<ChangeItem> list) {
            this.tryRefreshAppenders(list);
        }

        private void tryRefreshAppenders(List<ChangeItem> list) {
            if (Utils.isOutputPropertiesChange(list)) {
                appenderManager.refresh();
            }
        }
    }

    public class Reporter implements com.megaease.easeagent.plugin.api.Reporter, com.megaease.easeagent.plugin.api.config.ConfigChangeListener {
        private final String domain;
        private volatile MetricProps metricProps;
        private volatile KeySender sender;

        public Reporter(Config config) {
            this.domain = config.domain();
            this.metricProps = Utils.extractMetricProps(config);
            this.sender = newKeyLogger();
            config.addChangeListener(this);
        }

        public void report(String context) {
            sender.send(context);
        }

        private KeySender newKeyLogger() {
            return new KeySender(domain, PluginMetricReporterImpl.this.appenderManager, metricProps);
        }

        @Override
        public void onChange(Config oldConfig, Config newConfig) {
            MetricProps newProps = Utils.extractMetricProps(newConfig);
            if (metricProps.getTopic().equals(newProps.getTopic()) && metricProps.getAppendType().equals(newProps.getAppendType())) {
                return;
            }
            this.metricProps = newProps;
            this.sender = newKeyLogger();
        }
    }
}
