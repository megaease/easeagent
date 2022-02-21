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

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricName;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.tools.metrics.GaugeMetricModel;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.*;
import java.util.function.Supplier;

public class MetricRegistryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetricRegistryService.class);
    public static final MetricRegistryService DEFAULT = new MetricRegistryService();

    private static final List<MetricRegistry> REGISTRY_LIST = new ArrayList<>();

    public MetricRegistry createMetricRegistry(Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
        MetricRegistry registry = new MetricRegistry();
        REGISTRY_LIST.add(registry);
        EaseAgentSampleBuilder easeAgentSampleBuilder = new EaseAgentSampleBuilder(additionalAttributes, tags);
        new GaugeDropwizardExports(registry, (name, metric) -> metric instanceof Gauge, easeAgentSampleBuilder).register();
        new DropwizardExports(registry, (name, metric) -> !(metric instanceof Gauge), easeAgentSampleBuilder).register();
        return registry;
    }

    class EaseAgentSampleBuilder extends DefaultSampleBuilder {
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
            additionalLabelNames.add(Tags.CATEGORY);
            additionalLabelValues.add(tags.getCategory());
            additionalLabelNames.add(Tags.TYPE);
            additionalLabelValues.add(tags.getType());
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
            return super.createSample(rebuildName(dropwizardName), nameSuffix, newAdditionalLabelNames, newAdditionalLabelValues, value);
        }

        private String rebuildName(String name) {
            try {
                MetricName metricName = MetricName.metricNameFor(name);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(metricName.getMetricType());
                stringBuilder.append(".");
                stringBuilder.append(metricName.getMetricSubType());
                stringBuilder.append(".");
                stringBuilder.append(metricName.getKey());
                return stringBuilder.toString();
            } catch (Exception e) {
                LOGGER.error("rebuild metric name[{}] fail.{}", name, e);
                return name;
            }
        }

    }

    public static class GaugeDropwizardExports extends Collector implements Collector.Describable {
        protected static final String KEY_LABEL_NAME = "value_key";
        private MetricRegistry registry;
        private MetricFilter metricFilter;
        private SampleBuilder sampleBuilder;

        public GaugeDropwizardExports(MetricRegistry registry, MetricFilter metricFilter, SampleBuilder sampleBuilder) {
            this.registry = registry;
            this.metricFilter = metricFilter;
            this.sampleBuilder = sampleBuilder;
        }


        private static String getHelpMessage(String metricName) {
            return String.format("Generated from Dropwizard metric import (metric=%s, type=%s)", metricName, Gauge.class.getName());
        }


        public List<MetricFamilySamples> collect() {
            Map<String, MetricFamilySamples> mfSamplesMap = new HashMap();
            Iterator var2 = this.registry.getGauges(this.metricFilter).entrySet().iterator();

            Map.Entry entry;
            while (var2.hasNext()) {
                entry = (Map.Entry) var2.next();
                Gauge gauge = (Gauge) entry.getValue();
                Object gaugeValue = gauge.getValue();
                if (gaugeValue instanceof GaugeMetricModel) {
                    this.addToMap(mfSamplesMap, this.fromGaugeModel((String) entry.getKey(), (GaugeMetricModel) gaugeValue));
                } else {
                    this.addToMap(mfSamplesMap, this.fromGauge((String) entry.getKey(), gaugeValue));
                }
            }
            return new ArrayList(mfSamplesMap.values());
        }

        MetricFamilySamples fromGaugeModel(String dropwizardName, GaugeMetricModel gauge) {
            Map<String, Object> values = gauge.toHashMap();
            if (values == null || values.isEmpty()) {
                return null;
            }
            List<MetricFamilySamples.Sample> samples = new ArrayList<>();
            String name = "";
            for (Map.Entry<String, Object> entry : gauge.toHashMap().entrySet()) {
                MetricFamilySamples.Sample sample = fromGaugeSample(dropwizardName, entry.getValue());
                sample.labelNames.add(KEY_LABEL_NAME);
                sample.labelValues.add(entry.getKey());
                samples.add(sample);
                name = sample.name;
            }
            return new MetricFamilySamples(name, Type.GAUGE, getHelpMessage(dropwizardName), samples);
        }

        private MetricFamilySamples.Sample fromGaugeSample(String dropwizardName, Object gaugeValue) {
            double value;
            if (gaugeValue instanceof Number) {
                value = ((Number) gaugeValue).doubleValue();
            } else {
                if (!(gaugeValue instanceof Boolean)) {
                    LOGGER.warn(String.format("Invalid type for Gauge %s: %s", sanitizeMetricName(dropwizardName), gaugeValue == null ? "null" : gaugeValue.getClass().getName()));
                    return null;
                }

                value = (Boolean) gaugeValue ? 1.0D : 0.0D;
            }
            return this.sampleBuilder.createSample(dropwizardName, "", new ArrayList(), new ArrayList(), value);
        }

        private MetricFamilySamples fromGauge(String dropwizardName, Object gaugeValue) {
            MetricFamilySamples.Sample sample = fromGaugeSample(dropwizardName, gaugeValue);
            return new MetricFamilySamples(sample.name, Type.GAUGE, getHelpMessage(dropwizardName), Arrays.asList(sample));
        }

        private void addToMap(Map<String, MetricFamilySamples> mfSamplesMap, MetricFamilySamples newMfSamples) {
            if (newMfSamples != null) {
                MetricFamilySamples currentMfSamples = (MetricFamilySamples) mfSamplesMap.get(newMfSamples.name);
                if (currentMfSamples == null) {
                    mfSamplesMap.put(newMfSamples.name, newMfSamples);
                } else {
                    List<MetricFamilySamples.Sample> samples = new ArrayList(currentMfSamples.samples);
                    samples.addAll(newMfSamples.samples);
                    mfSamplesMap.put(newMfSamples.name, new MetricFamilySamples(newMfSamples.name, currentMfSamples.type, currentMfSamples.help, samples));
                }
            }

        }

        public List<MetricFamilySamples> describe() {
            return new ArrayList();
        }
    }

}
