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
import com.megaease.easeagent.plugin.api.config.ChangeItem;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.config.PluginConfigChangeListener;
import com.megaease.easeagent.report.OutputProperties;
import com.megaease.easeagent.report.metric.log4j.AppenderManager;
import com.megaease.easeagent.report.util.Utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MetricReporterImpl implements MetricReporter {
    private final ConcurrentHashMap<String, Reporter> reporters;
    private final Configs configs;
    private final AppenderManager appenderManager;

    public MetricReporterImpl(Configs configs) {
        this.reporters = new ConcurrentHashMap<>();
        OutputProperties outputProperties = Utils.extractOutputProperties(configs);
        this.appenderManager = AppenderManager.create(outputProperties);
        this.configs = configs;
        configs.addChangeListener(this);
    }

    public static MetricReporter create(Configs config) {
        return new MetricReporterImpl(config);
    }

    @Override
    public com.megaease.easeagent.plugin.api.Reporter reporter(IPluginConfig config) {
        Reporter reporter = reporters.get(config.namespace());
        if (reporter != null) {
            return reporter;
        }
        synchronized (reporters) {
            reporter = reporters.get(config.namespace());
            if (reporter != null) {
                return reporter;
            }
            reporter = new Reporter(config);
            reporters.put(config.namespace(), reporter);
            return reporter;
        }
    }

    @Override
    public void onChange(List<ChangeItem> list) {
        if (Utils.isOutputPropertiesChange(list)) {
            appenderManager.refresh();
        }
    }

    public class Reporter implements com.megaease.easeagent.plugin.api.Reporter, PluginConfigChangeListener {
        private final String namespace;
        private MetricProps metricProps;
        private KeySender sender;

        public Reporter(IPluginConfig config) {
            this.namespace = config.namespace();
            this.metricProps = Utils.extractMetricProps(config);
            this.sender = newKeyLogger();
            config.addChangeListener(this);
        }

        public void report(String context) {
            sender.send(context);
        }

        private KeySender newKeyLogger() {
            return new KeySender(namespace, MetricReporterImpl.this.appenderManager, metricProps);
        }

        @Override
        public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
            MetricProps newProps = Utils.extractMetricProps(newConfig);
            if (metricProps.getTopic().equals(newProps.getTopic()) && metricProps.getAppendType().equals(newProps.getAppendType())) {
                return;
            }
            this.metricProps = newProps;
            this.sender = newKeyLogger();
        }
    }
}
