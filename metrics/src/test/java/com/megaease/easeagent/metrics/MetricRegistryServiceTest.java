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

package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.EaseAgentPrometheusExports;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.tools.metrics.GaugeMetricModel;
import com.megaease.easeagent.plugin.utils.ImmutableMap;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class MetricRegistryServiceTest {

    private final String typeMatch = "testType_";

    private MetricRegistry reset(NameFactory nameFactory) {
        CollectorRegistry.defaultRegistry.clear();
        List<KeyType> keyTypes = MetricProviderImpl.keyTypes(nameFactory);
        Tags tags = new Tags("testCategory", "testType", "testName").put("testKey", "testValue");
        Supplier<Map<String, Object>> additionalAttributes = () -> Collections.singletonMap("additionalAttributesKey", "additionalAttributesValue");
        ConverterAdapter converterAdapter = new ConverterAdapter(nameFactory, keyTypes,
            additionalAttributes, tags);
        MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry(
            converterAdapter,
            additionalAttributes,
            tags
        );
        for (String s : metricRegistry.getNames()) {
            metricRegistry.remove(s);
        }
        return metricRegistry;
    }

    @Test
    public void createMetricRegistry() {
        NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.NONE,
            ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount).build()
        ).gaugeType(MetricSubType.DEFAULT, new HashMap<>()).build();
        String name = nameFactory.counterName("GET tt", MetricSubType.NONE);
        MetricRegistry metricRegistry = reset(nameFactory);

        metricRegistry.counter(name).inc();
        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples s = samples.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.contains("additionalAttributesKey"));
                assertTrue(sample.labelNames.contains("testKey"));
                assertTrue(sample.labelValues.contains("additionalAttributesValue"));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_TYPE_LABEL_NAME));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_SUB_TYPE_LABEL_NAME));
                assertTrue(sample.labelValues.contains(MetricType.CounterType.name()));
                assertTrue(sample.labelValues.contains(MetricSubType.NONE.name()));
                assertTrue(sample.name.contains("testCategory_"));
                assertTrue(sample.name.contains(typeMatch));
            }
        }

        for (String s : metricRegistry.getNames()) {
            metricRegistry.remove(s);
        }
        List<KeyType> keyTypes = MetricProviderImpl.keyTypes(nameFactory);
        Tags tags = new Tags("testCategory", "testType", "testName").put("testKey", "testValue");
        Supplier<Map<String, Object>> additionalAttributes = () -> Collections.singletonMap("additionalAttributesKey", "additionalAttributesValue");
        ConverterAdapter converterAdapter = new ConverterAdapter(nameFactory, keyTypes,
            additionalAttributes, tags);
        metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry(
            converterAdapter,
            null,
            tags
        );
        metricRegistry.counter(name).inc();
        samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples s = samples.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertEquals(4, sample.labelNames.size());
                assertEquals(4, sample.labelValues.size());
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_TYPE_LABEL_NAME));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_SUB_TYPE_LABEL_NAME));
                assertTrue(sample.labelValues.contains(MetricType.CounterType.name()));
                assertTrue(sample.labelValues.contains(MetricSubType.NONE.name()));
            }
        }


    }

    @Test
    public void testCounter() {
        NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.DEFAULT,
            ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount).build()
        ).counterType(MetricSubType.ERROR,
            ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount).build()
        ).build();
        MetricRegistry metricRegistry = reset(nameFactory);
        String name = nameFactory.counterName("GET tt", MetricSubType.DEFAULT);
        metricRegistry.counter(name).inc();
        String errorName = nameFactory.counterName("GET tt", MetricSubType.ERROR);
        metricRegistry.counter(errorName);
        Enumeration<Collector.MetricFamilySamples> samplesEnumeration = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        List<Collector.MetricFamilySamples> samples = new ArrayList<>();
        while (samplesEnumeration.hasMoreElements()) {
            Collector.MetricFamilySamples s = samplesEnumeration.nextElement();
            samples.add(s);
        }
        assertEquals(2, samples.size());
        for (Collector.MetricFamilySamples s : samples) {
            assertEquals(1, s.samples.size());
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_TYPE_LABEL_NAME));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_SUB_TYPE_LABEL_NAME));
                assertTrue(sample.labelValues.contains(MetricType.CounterType.name()));

                assertTrue(sample.name.contains("testCategory_"));
                assertTrue(sample.name.contains(typeMatch));

//                assertTrue(sample.labelNames.contains(EaseAgentPrometheusExports.VALUE_TYPE_LABEL_NAME));

                if (sample.labelValues.contains(MetricSubType.DEFAULT.name())) {
                    assertTrue(sample.name.endsWith(MetricField.EXECUTION_COUNT.getField()));
                    assertEquals(1, (int) sample.value);
                } else {
                    assertTrue(sample.labelValues.contains(MetricSubType.ERROR.name()));
                    assertTrue(sample.name.endsWith(MetricField.EXECUTION_ERROR_COUNT.getField()));
                    assertEquals(0, (int) sample.value);
                }
            }
        }
    }

    @Test
    public void testTimer() {
        Map<MetricField, MetricValueFetcher> valueFetchers = ImmutableMap.<MetricField, MetricValueFetcher>builder()
            .put(MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
            .put(MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
            .put(MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
            .put(MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
            .put(MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
            .put(MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
            .put(MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
            .put(MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
            .put(MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
            .put(MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
            .build();
        NameFactory nameFactory = NameFactory.createBuilder().timerType(MetricSubType.DEFAULT,
            valueFetchers)
            .build();
        List<String> labelValues = labelValues(valueFetchers);
        MetricRegistry metricRegistry = reset(nameFactory);
        String name = nameFactory.timerName("GET tt", MetricSubType.DEFAULT);
        metricRegistry.timer(name).update(1000, TimeUnit.MILLISECONDS);
        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        int size = 0;
        while (samples.hasMoreElements()) {
            size++;
            Collector.MetricFamilySamples s = samples.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_TYPE_LABEL_NAME));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_SUB_TYPE_LABEL_NAME));
                assertTrue(sample.labelValues.contains(MetricType.TimerType.name()));
                assertTrue(sample.labelValues.contains(MetricSubType.DEFAULT.name()));
                assertTrue(sample.name.contains("testCategory_"));
                assertTrue(sample.name.contains(typeMatch));


//                assertTrue(sample.labelNames.contains(EaseAgentPrometheusExports.VALUE_TYPE_LABEL_NAME));
                assertTrue(endWithOnce(sample.name, labelValues));
                assertEquals(1000, (int) sample.value);
            }
        }
        assertEquals(10, size);
    }

    public static boolean endWithOnce(String name, List<String> ends) {
        Iterator<String> iterator = ends.iterator();
        while (iterator.hasNext()) {
            String end = iterator.next();
            if (name.endsWith(end)) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private List<String> labelValues(Map<MetricField, MetricValueFetcher> valueFetchers) {
        List<String> values = new ArrayList<>();
        for (MetricField metricField : valueFetchers.keySet()) {
            values.add(metricField.getField());
        }
        return values;
    }

    @Test
    public void testMeter() {
        Map<MetricField, MetricValueFetcher> valueFetchers = ImmutableMap.<MetricField, MetricValueFetcher>builder()
            .put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
            .put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
            .put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
            .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
            .build();
        NameFactory nameFactory = NameFactory.createBuilder().meterType(MetricSubType.ERROR,
            valueFetchers)
            .build();
        List<String> labelValues = labelValues(valueFetchers);
        MetricRegistry metricRegistry = reset(nameFactory);
        String name = nameFactory.meterName("GET tt", MetricSubType.ERROR);
        metricRegistry.meter(name).mark();
        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        int size = 0;
        while (samples.hasMoreElements()) {
            size++;
            Collector.MetricFamilySamples s = samples.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_TYPE_LABEL_NAME));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_SUB_TYPE_LABEL_NAME));
                assertTrue(sample.labelValues.contains(MetricType.MeterType.name()));
                assertTrue(sample.labelValues.contains(MetricSubType.ERROR.name()));
                assertTrue(sample.name.contains("testCategory_"));
                assertTrue(sample.name.contains(typeMatch));


//                assertTrue(sample.labelNames.contains(EaseAgentPrometheusExports.VALUE_TYPE_LABEL_NAME));
//                assertTrue(labelValues.contains(sample.labelValues.get(0)));
                assertTrue(endWithOnce(sample.name, labelValues));
            }
        }
        assertEquals(4, size);
    }

    @Test
    public void testHistogram() {
        // Temporarily unsupported
        // Please use timer to calculate the time of P95, P99, etc
    }


    @Test
    public void testGauge() {
        NameFactory nameFactory = NameFactory.createBuilder().gaugeType(MetricSubType.DEFAULT, new HashMap<>()).build();
        MetricRegistry metricRegistry = reset(nameFactory);

        String gaugeName1 = "GETtt1";
        String gaugeName = nameFactory.gaugeName(gaugeName1, MetricSubType.DEFAULT);
        String key = "testGaugeKey1";
        String key2 = "testGaugeKey2";
        int value = 101;
        int value2 = 102;

        metricRegistry.gauge(gaugeName, () -> () -> new TestGaugeModel().put(key, value).put(key2, value2));

        int value3 = 103;
        String gaugeName2 = "GETtt2";
        gaugeName = nameFactory.gaugeName(gaugeName2, MetricSubType.DEFAULT);
        metricRegistry.gauge(gaugeName, () -> () -> value3);

        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples s = samples.nextElement();
            assertTrue(s.name.contains(typeMatch));
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_TYPE_LABEL_NAME));
                assertTrue(sample.labelNames.contains(MetricRegistryService.METRIC_SUB_TYPE_LABEL_NAME));
                assertTrue(sample.labelValues.contains(MetricType.GaugeType.name()));
                assertTrue(sample.labelValues.contains(MetricSubType.DEFAULT.name()));
                assertTrue(sample.name.contains("testCategory_"));
                assertTrue(sample.name.contains(typeMatch));


//                    assertTrue(sample.labelNames.contains(EaseAgentPrometheusExports.VALUE_TYPE_LABEL_NAME));
                int sampleIntValue = (int) sample.value;
                if (sample.name.endsWith(key)) {
                    assertEquals(value, sampleIntValue);
                } else if (sample.name.endsWith(key2)) {
                    assertEquals(value2, sampleIntValue);
                } else {
//                        assertTrue(sample.labelNames.contains(EaseAgentPrometheusExports.VALUE_TYPE_LABEL_NAME));
//                        assertTrue(sample.labelValues.contains("value"));
                    assertTrue(sample.name.endsWith("value"));
                    assertEquals(value3, (int) sample.value);
                }
            }

        }
    }


    public static class TestGaugeModel implements GaugeMetricModel {
        private final Map<String, Object> data = new HashMap<>();

        public TestGaugeModel put(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @Override
        public Map<String, Object> toHashMap() {
            return data;
        }
    }
}
