/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.metrics.converter;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.metric.name.MetricName;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

import java.util.*;

public class EaseAgentPrometheusExports extends Collector implements Collector.Describable {
    private static final Logger LOGGER = LoggerFactory.getLogger(EaseAgentPrometheusExports.class);
    public static final String VALUE_TYPE_LABEL_NAME = "value_type";
    private final MetricRegistry registry;
    private final AbstractConverter abstractConverter;
    private final MetricFilter metricFilter = MetricFilter.ALL;
    private final SampleBuilder sampleBuilder;
    private final CounterExports counterExports = new CounterExports();
    private final MeterExports meterExports = new MeterExports();
    private final TimerExports timerExports = new TimerExports();
    private final HistogramExports histogramExports = new HistogramExports();
    private final GaugeExports gaugeExports = new GaugeExports();


    public EaseAgentPrometheusExports(MetricRegistry registry, AbstractConverter abstractConverter, SampleBuilder sampleBuilder) {
        this.registry = registry;
        this.abstractConverter = abstractConverter;
        this.sampleBuilder = sampleBuilder;
    }

    private static String getHelpMessage(String metricName, Class<?> clzss) {
        return String.format("Generated from Dropwizard metric import (metric=%s, type=%s)", metricName, clzss.getName());
    }

    MetricFamilySamples.Sample doubleValue(String dropwizardName, Object obj, String valueType, Class<?> clzss) {
        double value;
        if (obj instanceof Number) {
            value = ((Number) obj).doubleValue();
        } else {
            if (!(obj instanceof Boolean)) {
                LOGGER.warn(String.format("Invalid type for %s %s: %s", clzss.getSimpleName(), sanitizeMetricName(dropwizardName), obj == null ? "null" : clzss.getName()));
                return null;
            }

            value = (Boolean) obj ? 1.0D : 0.0D;
        }

        return this.sampleBuilder.createSample(dropwizardName, "", Arrays.asList(VALUE_TYPE_LABEL_NAME), Arrays.asList(valueType), value);
    }

    public MetricFilter getMetricFilter() {
        return metricFilter;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        Map<String, MetricFamilySamples> mfSamplesMap = new HashMap<>();

        gaugeExports.addToMap(mfSamplesMap);
        counterExports.addToMap(mfSamplesMap);
        meterExports.addToMap(mfSamplesMap);
        timerExports.addToMap(mfSamplesMap);
        histogramExports.addToMap(mfSamplesMap);
        return new ArrayList<>(mfSamplesMap.values());
    }

    protected void addToMap(Map<String, MetricFamilySamples> mfSamplesMap, MetricFamilySamples newMfSamples) {
        if (newMfSamples != null) {
            MetricFamilySamples currentMfSamples = mfSamplesMap.get(newMfSamples.name);
            if (currentMfSamples == null) {
                mfSamplesMap.put(newMfSamples.name, newMfSamples);
            } else {
                List<MetricFamilySamples.Sample> samples = new ArrayList<>(currentMfSamples.samples);
                samples.addAll(newMfSamples.samples);
                mfSamplesMap.put(newMfSamples.name, new MetricFamilySamples(newMfSamples.name, currentMfSamples.type, currentMfSamples.help, samples));
            }
        }

    }

    public List<MetricFamilySamples> describe() {
        return new ArrayList<>();
    }

    abstract class Exports<T extends Metric> {
        private final Collector.Type type;
        private final Class<?> clzss;

        public Exports(Type type, Class<?> clzss) {
            this.type = type;
            this.clzss = clzss;
        }

        public void addToMap(Map<String, MetricFamilySamples> mfSamplesMap) {
            Map<String, Object> values = new HashMap<>();
            SortedMap<String, T> gaugeSortedMap = getMetric();
            for (String s : gaugeSortedMap.keySet()) {
                writeValue(MetricName.metricNameFor(s), gaugeSortedMap, values);
                List<MetricFamilySamples.Sample> samples = new ArrayList<>();
                String name = null;
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    MetricFamilySamples.Sample sample = doubleValue(s, entry.getValue(), entry.getKey(), clzss);
                    if (sample == null) {
                        continue;
                    }
                    name = sample.name;
                    samples.add(sample);
                }
                if (name != null) {
                    MetricFamilySamples metricFamilySamples = new MetricFamilySamples(name, type, getHelpMessage(s, clzss), samples);
                    EaseAgentPrometheusExports.this.addToMap(mfSamplesMap, metricFamilySamples);
                }
                values.clear();
            }
        }

        protected abstract SortedMap<String, T> getMetric();

        protected abstract void writeValue(MetricName metricName, SortedMap<String, T> metric, Map<String, Object> values);
    }


    class CounterExports extends Exports<Counter> {

        public CounterExports() {
            super(Type.COUNTER, Counter.class);
        }

        @Override
        protected SortedMap<String, Counter> getMetric() {
            return registry.getCounters(metricFilter);
        }

        @Override
        protected void writeValue(MetricName metricName, SortedMap<String, Counter> metric, Map<String, Object> values) {
            abstractConverter.writeCounters(metricName.getKey(), metricName.getMetricSubType(), metric, values);
        }
    }

    class MeterExports extends Exports<Meter> {

        public MeterExports() {
            super(Type.COUNTER, Meter.class);
        }

        @Override
        protected SortedMap<String, Meter> getMetric() {
            return registry.getMeters(metricFilter);
        }

        @Override
        protected void writeValue(MetricName metricName, SortedMap<String, Meter> metric, Map<String, Object> values) {
            abstractConverter.writeMeters(metricName.getKey(), metricName.getMetricSubType(), metric, values);
        }
    }

    class TimerExports extends Exports<Timer> {

        public TimerExports() {
            super(Type.SUMMARY, Timer.class);
        }

        @Override
        protected SortedMap<String, Timer> getMetric() {
            return registry.getTimers(metricFilter);
        }

        @Override
        protected void writeValue(MetricName metricName, SortedMap<String, Timer> metric, Map<String, Object> values) {
            abstractConverter.writeTimers(metricName.getKey(), metricName.getMetricSubType(), metric, values);
        }
    }

    class HistogramExports extends Exports<Histogram> {

        public HistogramExports() {
            super(Type.SUMMARY, Histogram.class);
        }

        @Override
        protected SortedMap<String, Histogram> getMetric() {
            return registry.getHistograms(metricFilter);
        }

        @Override
        protected void writeValue(MetricName metricName, SortedMap<String, Histogram> metric, Map<String, Object> values) {
            abstractConverter.writeHistograms(metricName.getKey(), metricName.getMetricSubType(), metric, values);
        }
    }

    class GaugeExports extends Exports<Gauge> {

        public GaugeExports() {
            super(Type.GAUGE, Gauge.class);
        }

        @Override
        protected SortedMap<String, Gauge> getMetric() {
            return registry.getGauges(metricFilter);
        }

        @Override
        protected void writeValue(MetricName metricName, SortedMap<String, Gauge> metric, Map<String, Object> values) {
            abstractConverter.writeGauges(metricName.getKey(), metricName.getMetricSubType(), metric, values);
        }
    }
}
