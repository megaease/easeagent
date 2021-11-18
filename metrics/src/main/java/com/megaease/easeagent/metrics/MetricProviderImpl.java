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

package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.config.ConfigAware;
import com.megaease.easeagent.core.MetricProvider;
import com.megaease.easeagent.metrics.config.MetricsConfig;
import com.megaease.easeagent.metrics.config.PluginMetricsConfig;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.metrics.converter.MetricsAdditionalAttributes;
import com.megaease.easeagent.metrics.impl.MetricRegistryImpl;
import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;
import com.megaease.easeagent.plugin.api.metric.name.MetricType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.report.AgentReport;
import com.megaease.easeagent.report.AgentReportAware;
import com.megaease.easeagent.report.PluginMetricReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MetricProviderImpl implements AgentReportAware, ConfigAware, MetricProvider {
    private Config config;
    private AgentReport agentReport;
    private Supplier<Map<String, Object>> additionalAttributes;


    @Override
    public void setConfig(Config config) {
        this.config = config;
        this.additionalAttributes = new MetricsAdditionalAttributes(config);
    }

    @Override
    public void setAgentReport(AgentReport report) {
        this.agentReport = report;
    }


    @Override
    public MetricRegistrySupplier metricSupplier() {
        return new ApplicationMetricRegistrySupplier();
    }

    public class ApplicationMetricRegistrySupplier implements MetricRegistrySupplier {

        @Override
        public com.megaease.easeagent.plugin.api.metric.MetricRegistry newMetricRegistry(
            com.megaease.easeagent.plugin.api.config.Config config,
            NameFactory nameFactory, Tags tags) {
            MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry();
            MetricsConfig metricsConfig = new PluginMetricsConfig(config);
            List<KeyType> keyTypes = new ArrayList<>();
            for (MetricType metricType : nameFactory.metricTypes()) {
                switch (metricType) {
                    case TimerType:
                        keyTypes.add(KeyType.Timer);
                        break;
                    case GaugeType:
                        keyTypes.add(KeyType.Gauge);
                        break;
                    case MeterType:
                        keyTypes.add(KeyType.Meter);
                        break;
                    case CounterType:
                        keyTypes.add(KeyType.Counter);
                        break;
                    case HistogramType:
                        keyTypes.add(KeyType.Histogram);
                        break;
                    default:
                        break;
                }
            }
            ConverterAdapter converterAdapter = new ConverterAdapter(nameFactory, keyTypes,
                MetricProviderImpl.this.additionalAttributes, tags);
            Reporter reporter = agentReport.pluginMetricReporter().reporter(config);
            new AutoRefreshReporter(metricRegistry, metricsConfig,
                converterAdapter,
                s -> reporter.report(s)).run();
            return MetricRegistryImpl.build(metricRegistry);
        }

        @Override
        public Reporter reporter(com.megaease.easeagent.plugin.api.config.Config config) {
            return agentReport.pluginMetricReporter().reporter(config);
        }
    }
}
