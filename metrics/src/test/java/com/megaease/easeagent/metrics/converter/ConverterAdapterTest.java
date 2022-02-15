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

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.megaease.easeagent.metrics.impl.MetricRegistryMock;
import com.megaease.easeagent.metrics.impl.MetricTestUtils;
import com.megaease.easeagent.mock.config.ConfigMock;
import com.megaease.easeagent.plugin.api.metric.ServiceMetric;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.tools.metrics.ServerMetric;
import com.megaease.easeagent.plugin.utils.ImmutableMap;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.megaease.easeagent.plugin.api.metric.name.MetricField.EXECUTION_COUNT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConverterAdapterTest {
    NameFactory nameFactory = NameFactory.createBuilder()
        .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
            .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
            .build())
        .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
            .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
            .put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
            .put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
            .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
            .build())
        .histogramType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
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
            .build())
        .gaugeType(MetricSubType.DEFAULT, new HashMap<>())
        .timerType(MetricSubType.DEFAULT,
            ImmutableMap.<MetricField, MetricValueFetcher>builder()
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
                .build())
        .build();

    public static String buildMetricName(String name) {
        return ConverterAdapterTest.class.getName() + "##" + name;
    }

    class KeysFromMetricsRun {
        ConverterAdapter converter;
        SortedMap<String, Gauge> gauges;
        SortedMap<String, Counter> counters;
        SortedMap<String, Histogram> histograms;
        SortedMap<String, Meter> meters;
        SortedMap<String, Timer> timer;

        public KeysFromMetricsRun setConverter(ConverterAdapter converter) {
            this.converter = converter;
            return this;
        }

        public KeysFromMetricsRun setGauges(SortedMap<String, Gauge> gauges) {
            this.gauges = gauges;
            return this;
        }

        public KeysFromMetricsRun setCounters(SortedMap<String, Counter> counters) {
            this.counters = counters;
            return this;
        }

        public KeysFromMetricsRun setHistograms(SortedMap<String, Histogram> histograms) {
            this.histograms = histograms;
            return this;
        }

        public KeysFromMetricsRun setMeters(SortedMap<String, Meter> meters) {
            this.meters = meters;
            return this;
        }

        public KeysFromMetricsRun setTimer(SortedMap<String, Timer> timer) {
            this.timer = timer;
            return this;
        }

        public List<String> doit() {
            return converter.keysFromMetrics(gauges, counters, histograms, meters, timer);
        }
    }

    private ConverterAdapter createAllTypeConverterAdapter() {
        return new ConverterAdapter(
            nameFactory,
            Arrays.asList(KeyType.values()),
            new MetricsAdditionalAttributes(
                ConfigMock.getCONFIGS()),
            new Tags("testCategory", "testType", "testKeyFieldName"));
    }

    @Test
    public void keysFromMetrics() {

        ConverterAdapter converter = createAllTypeConverterAdapter();

        String key = buildMetricName("testConvertMap");
        String key2 = buildMetricName("testConvertMap2");
        String gaugeName = nameFactory.gaugeName(key, MetricSubType.DEFAULT);
        String counterName = nameFactory.counterName(key, MetricSubType.DEFAULT);
        String histogramName = nameFactory.histogramName(key, MetricSubType.DEFAULT);
        String meterName = nameFactory.meterName(key, MetricSubType.DEFAULT);
        String timerName = nameFactory.timerName(key, MetricSubType.DEFAULT);
        String timerName2 = nameFactory.timerName(key2, MetricSubType.DEFAULT);
        KeysFromMetricsRun keysFromMetricsRun = new KeysFromMetricsRun()
            .setConverter(converter)
            .setGauges(new TreeMap<>(Collections.singletonMap(gaugeName,
                MetricRegistryMock.getCodahaleMetricRegistry().gauge(gaugeName, () -> () -> "gaugeValue"))
            ))
            .setCounters(new TreeMap<>(Collections.singletonMap(counterName,
                MetricRegistryMock.getCodahaleMetricRegistry().counter(counterName))
            ))
            .setHistograms(new TreeMap<>(Collections.singletonMap(histogramName,
                MetricRegistryMock.getCodahaleMetricRegistry().histogram(histogramName))
            ))
            .setMeters(new TreeMap<>(Collections.singletonMap(meterName,
                MetricRegistryMock.getCodahaleMetricRegistry().meter(meterName))
            ))
            .setTimer(new TreeMap<>(Collections.singletonMap(timerName,
                MetricRegistryMock.getCodahaleMetricRegistry().timer(timerName))
            ));

        List<String> result = keysFromMetricsRun.doit();
        assertEquals(1, result.size());
        assertEquals(key, result.get(0));

        SortedMap<String, Timer> timerMap = new TreeMap<>();
        timerMap.put(timerName, MetricRegistryMock.getCodahaleMetricRegistry().timer(timerName));
        timerMap.put(timerName2, MetricRegistryMock.getCodahaleMetricRegistry().timer(timerName2));
        keysFromMetricsRun.setTimer(timerMap);

        List<String> result2 = keysFromMetricsRun.doit();
        assertEquals(2, result2.size());
        assertTrue(result2.contains(key));
        assertTrue(result2.contains(key2));

        keysFromMetricsRun.setConverter(new ConverterAdapter(
            nameFactory,
            Arrays.asList(KeyType.Counter),
            new MetricsAdditionalAttributes(
                ConfigMock.getCONFIGS()),
            new Tags("testCategory", "testType", "testKeyFieldName")));


        List<String> result3 = keysFromMetricsRun.doit();
        assertEquals(1, result3.size());
        assertEquals(key, result3.get(0));

    }

    @Test
    public void writeGauges() {
        ConverterAdapter converter = createAllTypeConverterAdapter();

        String key = buildMetricName("writeGauges");
        String gaugeName = nameFactory.gaugeName(key, MetricSubType.DEFAULT);
        String value = "gaugeValue";
        SortedMap<String, Gauge> gauges = new TreeMap<>(Collections.singletonMap(gaugeName,
            MetricRegistryMock.getCodahaleMetricRegistry().gauge(gaugeName, () -> () -> value))
        );
        Map<String, Object> result = new HashMap<>();
        converter.writeGauges(key, gauges, result);
        assertEquals(value, result.get(gaugeName));
    }

    @Test
    public void writeCounters() {
        ConverterAdapter converter = createAllTypeConverterAdapter();

        String key = buildMetricName("writeCounters");
        String counterName = nameFactory.counterName(key, MetricSubType.DEFAULT);
        Counter counter = MetricRegistryMock.getCodahaleMetricRegistry().counter(counterName);
        SortedMap<String, Counter> counters = new TreeMap<>(Collections.singletonMap(counterName,
            counter)
        );
        counter.inc();
        Map<String, Object> result = new HashMap<>();
        converter.writeCounters(key, counters, result);
        assertEquals(1l, result.get(MetricField.EXECUTION_COUNT.getField()));
        counter.inc();

        converter.writeCounters(key, counters, result);
        assertEquals(2l, result.get(MetricField.EXECUTION_COUNT.getField()));
    }

    @Test
    public void writeHistograms() {
        ConverterAdapter converter = createAllTypeConverterAdapter();

        String key = buildMetricName("writeHistograms");
        String histogramName = nameFactory.histogramName(key, MetricSubType.DEFAULT);
        Histogram histogram = MetricRegistryMock.getCodahaleMetricRegistry().histogram(histogramName);
        for (int i = 0; i < 100; i++) {
            histogram.update(i);
        }
        SortedMap<String, Histogram> histograms = new TreeMap<>(Collections.singletonMap(histogramName,
            histogram)
        );
        Map<String, Object> result = new HashMap<>();
        converter.writeHistograms(key, histograms, result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void writeMeters() {
        ConverterAdapter converter = createAllTypeConverterAdapter();

        String key = buildMetricName("writeMeters");
        String meterName = nameFactory.meterName(key, MetricSubType.DEFAULT);
        Meter meter = MetricRegistryMock.getCodahaleMetricRegistry().meter(meterName);
        int count = 0;
        for (int i = 0; i < 15 * 60 / 5; i++) {
            meter.mark();
            count++;
            assertEquals(count, meter.getCount());
            meter.mark(10 + i);
            count += 10 + i;
            assertEquals(count, meter.getCount());
            MetricTestUtils.nextWindow(meter);
        }

        SortedMap<String, Meter> meters = new TreeMap<>(Collections.singletonMap(meterName,
            meter)
        );
        Map<String, Object> result = new HashMap<>();
        converter.writeMeters(key, meters, result);
        Object meanRateO = result.get(MetricField.MEAN_RATE.getField());
        Object m1O = result.get(MetricField.M1_RATE.getField());
        Object m5O = result.get(MetricField.M5_RATE.getField());
        Object m15O = result.get(MetricField.M15_RATE.getField());
        assertTrue(meanRateO instanceof Double);
        assertTrue(m1O instanceof Double);
        assertTrue(m5O instanceof Double);
        assertTrue(m15O instanceof Double);
        double meanRate = (double) meanRateO;
        double m1 = (double) m1O;
        double m5 = (double) m5O;
        double m15 = (double) m15O;

        assertTrue(String.format("meter.getMeanRate()<%s> must > 0", meanRate), meanRate > 0);
        assertTrue(String.format("meter.getOneMinuteRate()<%s> must > 0", m1), m1 > 0);
        assertTrue(String.format("meter.getFiveMinuteRate()<%s> must > 0", m5), m5 > 0);
        assertTrue(String.format("meter.getFifteenMinuteRate()<%s> must > 0", m15), m15 > 0);
        assertTrue(meanRate > m1);
        assertTrue(m1 > m5);
        assertTrue(m5 > m15);

    }

    @Test
    public void writeTimers() {
        ConverterAdapter converter = createAllTypeConverterAdapter();

        String key = buildMetricName("writeTimers");
        String timerName = nameFactory.timerName(key, MetricSubType.DEFAULT);
        Timer timer = MetricRegistryMock.getCodahaleMetricRegistry().timer(timerName);
        for (int i = 0; i < 100; i++) {
            timer.update(i, TimeUnit.MILLISECONDS);
        }
        SortedMap<String, Timer> timers = new TreeMap<>(Collections.singletonMap(timerName,
            timer)
        );
        Map<String, Object> result = new HashMap<>();
        converter.writeTimers(key, timers, result);

        assertEquals(0d, (double) result.get(MetricField.MIN_EXECUTION_TIME.getField()), 1);
        assertEquals(49.5d, (double) result.get(MetricField.MEAN_EXECUTION_TIME.getField()), 1);
        assertEquals(99d, (double) result.get(MetricField.MAX_EXECUTION_TIME.getField()), 1);

        assertEquals(24d, (double) result.get(MetricField.P25_EXECUTION_TIME.getField()), 1);
        assertEquals(49d, (double) result.get(MetricField.P50_EXECUTION_TIME.getField()), 1);
        assertEquals(74d, (double) result.get(MetricField.P75_EXECUTION_TIME.getField()), 1);
        assertEquals(94d, (double) result.get(MetricField.P95_EXECUTION_TIME.getField()), 1);
        assertEquals(97d, (double) result.get(MetricField.P98_EXECUTION_TIME.getField()), 1);
        assertEquals(98d, (double) result.get(MetricField.P99_EXECUTION_TIME.getField()), 1);
        assertEquals(99d, (double) result.get(MetricField.P999_EXECUTION_TIME.getField()),1);

    }
}
