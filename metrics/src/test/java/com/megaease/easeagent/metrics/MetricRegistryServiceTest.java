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
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.utils.ImmutableMap;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import org.junit.Test;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class MetricRegistryServiceTest {

    @Test
    public void createMetricRegistry() {
        NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.NONE,
            ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount).build()
        ).build();
        String name = nameFactory.counterName("GET tt", MetricSubType.NONE);
        MetricRegistry metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry(
            () -> Collections.singletonMap("additionalAttributesKey", "additionalAttributesValue"),
            new Tags("testCategory", "testType", "testName").put("testKey", "testValue")
        );
        for (String s : metricRegistry.getNames()) {
            metricRegistry.remove(s);
        }
        CollectorRegistry.defaultRegistry.clear();

        metricRegistry.counter(name).inc();
        Enumeration<Collector.MetricFamilySamples> samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples s = samples.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.contains("additionalAttributesKey"));
                assertTrue(sample.labelNames.contains(Tags.CATEGORY));
                assertTrue(sample.labelNames.contains(Tags.TYPE));
                assertTrue(sample.labelNames.contains("testKey"));
                assertTrue(sample.labelValues.contains("additionalAttributesValue"));
                assertTrue(sample.labelValues.contains("testCategory"));
                assertTrue(sample.labelValues.contains("testType"));
                assertTrue(sample.labelValues.contains("testValue"));
            }
        }
        for (String s : metricRegistry.getNames()) {
            metricRegistry.remove(s);
        }

        metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry(
            null,
            null
        );
        metricRegistry.counter(name).inc();
        samples = CollectorRegistry.defaultRegistry.filteredMetricFamilySamples(Collections.emptySet());
        while (samples.hasMoreElements()) {
            Collector.MetricFamilySamples s = samples.nextElement();
            for (Collector.MetricFamilySamples.Sample sample : s.samples) {
                assertTrue(sample.labelNames.isEmpty());
                assertTrue(sample.labelValues.isEmpty());
            }
        }
    }
}
