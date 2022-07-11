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

package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.metrics.converter.AbstractConverter;
import com.megaease.easeagent.metrics.converter.EaseAgentPrometheusExports;
import com.megaease.easeagent.plugin.api.metric.name.MetricName;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MetricRegistryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRegistryService.class);
    public static final String METRIC_TYPE_LABEL_NAME = "MetricType";
    public static final String METRIC_SUB_TYPE_LABEL_NAME = "MetricSubType";
    public static final MetricRegistryService DEFAULT = new MetricRegistryService();

    private static final List<MetricRegistry> REGISTRY_LIST = new ArrayList<>();

    public MetricRegistry createMetricRegistry(AbstractConverter abstractConverter, Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
        MetricRegistry registry = new MetricRegistry();
        REGISTRY_LIST.add(registry);
        EaseAgentSampleBuilder easeAgentSampleBuilder = new EaseAgentSampleBuilder(additionalAttributes, tags);
        EaseAgentPrometheusExports easeAgentPrometheusExports = new EaseAgentPrometheusExports(registry, abstractConverter, easeAgentSampleBuilder);
        easeAgentPrometheusExports.register();
        return registry;
    }

    static class EaseAgentSampleBuilder extends DefaultSampleBuilder {
        private final Supplier<Map<String, Object>> additionalAttributes;
        private final Tags tags;

        EaseAgentSampleBuilder(Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
            this.additionalAttributes = additionalAttributes;
            this.tags = tags;
        }

        private void additionalAttributes(List<String> additionalLabelNames, List<String> additionalLabelValues) {
            if (additionalAttributes == null) {
                return;
            }
            Map<String, Object> labels = additionalAttributes.get();
            if (labels == null || labels.isEmpty()) {
                return;
            }
            for (Map.Entry<String, Object> entry : labels.entrySet()) {
                additionalLabelNames.add(entry.getKey());
                additionalLabelValues.add(entry.getValue().toString());
            }
        }

        private void tags(List<String> additionalLabelNames, List<String> additionalLabelValues) {
            if (tags == null) {
                return;
            }
            Map<String, String> other = tags.getTags();
            if (other == null || other.isEmpty()) {
                return;
            }
            for (Map.Entry<String, String> entry : other.entrySet()) {
                additionalLabelNames.add(entry.getKey());
                additionalLabelValues.add(entry.getValue());
            }
        }

        @Override
        public Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value) {
            List<String> newAdditionalLabelNames = new ArrayList<>(additionalLabelNames);
            List<String> newAdditionalLabelValues = new ArrayList<>(additionalLabelValues);
            additionalAttributes(newAdditionalLabelNames, newAdditionalLabelValues);
            tags(newAdditionalLabelNames, newAdditionalLabelValues);
            return super.createSample(rebuildName(dropwizardName, newAdditionalLabelNames, newAdditionalLabelValues), nameSuffix, newAdditionalLabelNames, newAdditionalLabelValues, value);
        }

        private String rebuildName(String name, List<String> additionalLabelNames, List<String> additionalLabelValues) {
            try {
                MetricName metricName = MetricName.metricNameFor(name);
                StringBuilder stringBuilder = new StringBuilder();
                additionalLabelNames.add(METRIC_TYPE_LABEL_NAME);
                additionalLabelNames.add(METRIC_SUB_TYPE_LABEL_NAME);
                additionalLabelValues.add(metricName.getMetricType().name());
                additionalLabelValues.add(metricName.getMetricSubType().name());
                additionalLabelNames.add(tags.getKeyFieldName());
                additionalLabelValues.add(metricName.getKey());
                stringBuilder.append(tags.getCategory());
                stringBuilder.append(".");
                stringBuilder.append(tags.getType());
                return stringBuilder.toString();
            } catch (Exception e) {
                LOGGER.error("rebuild metric name[{}] fail.{}", name, e);
                return name;
            }
        }

    }

}
