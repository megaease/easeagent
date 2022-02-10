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

import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.MetricName;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;

import java.util.ArrayList;
import java.util.List;

public class MetricRegistryService {

    public static final MetricRegistryService DEFAULT = new MetricRegistryService();

    private static final List<MetricRegistry> REGISTRY_LIST = new ArrayList<>();

    public MetricRegistry createMetricRegistry() {
        MetricRegistry registry = new MetricRegistry();
        REGISTRY_LIST.add(registry);
        new DropwizardExports(registry, MetricFilter.ALL, new EaseAgentSampleBuilder()).register();
        return registry;
    }

    class EaseAgentSampleBuilder extends DefaultSampleBuilder {

        @Override
        public Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value) {
            return super.createSample(rebuildName(dropwizardName), nameSuffix, additionalLabelNames, additionalLabelValues, value);
        }

        private String rebuildName(String name) {
            MetricName metricName = MetricName.metricNameFor(name);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(metricName.getMetricType());
            stringBuilder.append(".");
            stringBuilder.append(metricName.getMetricSubType());
            stringBuilder.append(".");
            stringBuilder.append(metricName.getKey());
            return stringBuilder.toString();
        }

    }
}
