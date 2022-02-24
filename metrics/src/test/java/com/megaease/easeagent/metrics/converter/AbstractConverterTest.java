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
import com.megaease.easeagent.metrics.TestConst;
import com.megaease.easeagent.metrics.impl.MetricRegistryMock;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractConverterTest {
    public static String buildMetricName(String name) {
        return AbstractConverterTest.class.getName() + "##" + name;
    }

    public static class MockAbstractConverter extends AbstractConverter {
        AtomicInteger keysFromMetricsCount = new AtomicInteger();
        AtomicInteger writeGaugesCount = new AtomicInteger();
        AtomicInteger writeCountersCount = new AtomicInteger();
        AtomicInteger writeHistogramsCount = new AtomicInteger();
        AtomicInteger writeMetersCount = new AtomicInteger();
        AtomicInteger writeTimersCount = new AtomicInteger();

        MockAbstractConverter(Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
            super(additionalAttributes, tags);
        }

        @Override
        protected List<String> keysFromMetrics(SortedMap<String, Gauge> gauges,
                                               SortedMap<String, Counter> counters,
                                               SortedMap<String, Histogram> histograms,
                                               SortedMap<String, Meter> meters,
                                               SortedMap<String, Timer> timers) {
            keysFromMetricsCount.incrementAndGet();
            Set<String> set = new HashSet<>();
            set.addAll(gauges.keySet());
            set.addAll(counters.keySet());
            set.addAll(histograms.keySet());
            set.addAll(meters.keySet());
            set.addAll(timers.keySet());
            return new ArrayList<>(set);
        }

        @Override
        protected void writeGauges(String key, MetricSubType metricSubType, SortedMap<String, Gauge> gauges, Map<String, Object> output) {
            writeGaugesCount.incrementAndGet();
            output.put("writeGaugesKey", 1);
        }

        @Override
        protected void writeCounters(String key, MetricSubType metricSubType, SortedMap<String, Counter> counters, Map<String, Object> output) {
            writeCountersCount.incrementAndGet();
            output.put("writeCountersKey", 1);
        }

        @Override
        protected void writeHistograms(String key, MetricSubType metricSubType, SortedMap<String, Histogram> histograms, Map<String, Object> output) {
            writeHistogramsCount.incrementAndGet();
            output.put("writeHistogramsKey", 1);
        }

        @Override
        protected void writeMeters(String key, MetricSubType metricSubType, SortedMap<String, Meter> meters, Map<String, Object> output) {
            writeMetersCount.incrementAndGet();
            output.put("writeMetersKey", 1);
        }

        @Override
        protected void writeTimers(String key, MetricSubType metricSubType, SortedMap<String, Timer> timers, Map<String, Object> output) {
            writeTimersCount.incrementAndGet();
            output.put("writeTimersKey", 1);
        }
    }

    @Test
    public void convertMap() {
        MockAbstractConverter mockAbstractConverter = new MockAbstractConverter(
            new MetricsAdditionalAttributes(
                MockConfig.getCONFIGS()),
            new Tags("testCategory", "testType", "testKeyFieldName"));
        List<Map<String, Object>> result = mockAbstractConverter.convertMap(
            new TreeMap<>(Collections.singletonMap("testConvertMap",
                MetricRegistryMock.getCodahaleMetricRegistry().gauge(buildMetricName("convertMap#Gauge"), () -> () -> "gaugeValue"))
            ),
            new TreeMap<>(Collections.singletonMap("testConvertMap",
                MetricRegistryMock.getCodahaleMetricRegistry().counter(buildMetricName("convertMap#Counter")))
            ),
            new TreeMap<>(Collections.singletonMap("testConvertMap",
                MetricRegistryMock.getCodahaleMetricRegistry().histogram(buildMetricName("convertMap#Histogram")))
            ),
            new TreeMap<>(Collections.singletonMap("testConvertMap",
                MetricRegistryMock.getCodahaleMetricRegistry().meter(buildMetricName("convertMap#Meter")))
            ),
            new TreeMap<>(Collections.singletonMap("testConvertMap",
                MetricRegistryMock.getCodahaleMetricRegistry().timer(buildMetricName("convertMap#Timer")))
            )
        );
        assertEquals(1, mockAbstractConverter.keysFromMetricsCount.get());
        assertEquals(1, mockAbstractConverter.writeGaugesCount.get());
        assertEquals(1, mockAbstractConverter.writeCountersCount.get());
        assertEquals(1, mockAbstractConverter.writeHistogramsCount.get());
        assertEquals(1, mockAbstractConverter.writeMetersCount.get());
        assertEquals(1, mockAbstractConverter.writeTimersCount.get());
        assertEquals(1, result.size());
        Map<String, Object> data = result.get(0);
        assertEquals("testCategory", data.get(Tags.CATEGORY));
        assertEquals("testType", data.get(Tags.TYPE));
        assertEquals("testConvertMap", data.get("testKeyFieldName"));
        assertTrue(data.containsKey("timestamp"));
        assertEquals(TestConst.SERVICE_NAME, data.get(TestConst.SERVICE_KEY_NAME));
        assertEquals(TestConst.SERVICE_SYSTEM, data.get(ConfigConst.SYSTEM_NAME));
        assertEquals(1, data.get("writeGaugesKey"));
        assertEquals(1, data.get("writeCountersKey"));
        assertEquals(1, data.get("writeHistogramsKey"));
        assertEquals(1, data.get("writeMetersKey"));
        assertEquals(1, data.get("writeTimersKey"));

    }

    @Test
    public void writeGauges() {
        convertMap();
    }

    @Test
    public void writeCounters() {
        convertMap();
    }

    @Test
    public void writeHistograms() {
        convertMap();
    }

    @Test
    public void writeMeters() {
        convertMap();
    }

    @Test
    public void writeTimers() {
        convertMap();
    }

}
