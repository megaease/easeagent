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

package com.megaease.easeagent.metrics.impl;

import com.megaease.easeagent.metrics.MetricRegistryService;
import com.megaease.easeagent.plugin.api.metric.*;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MetricRegistryImplTest {
    String countName = buildMetricName("countName");
    String meterName = buildMetricName("meterName");
    String histogramName = buildMetricName("histogramName");
    String timerName = buildMetricName("timerName");
    String gaugeName = buildMetricName("gauge");
    com.codahale.metrics.MetricRegistry mr = MetricRegistryService.DEFAULT.createMetricRegistry(null, null, null);
    MetricRegistry metricRegistry = MetricRegistryImpl.build(mr);


    public static String buildMetricName(String name) {
        return MetricRegistryImplTest.class.getName() + "##" + name;
    }

    @Test
    public void build() {
        Assert.assertNotNull(metricRegistry);
    }

    @Test
    public void meter() {
        Meter meter = metricRegistry.meter(meterName);
        meter.mark();
        assertEquals(1, meter.getCount());
        System.out.println(meter.getFifteenMinuteRate() + "");
    }

    @Test
    public void remove() {
        String name = buildMetricName("test_remove");
        String value1 = "test_remove_value1";
        String value2 = "test_remove_value2";
        Gauge gauge = metricRegistry.gauge(name, () -> new TestGauge(value1));
        com.codahale.metrics.Gauge g = mr.gauge(name, () -> null);
        assertNotNull(gauge);
        assertNotNull(g);
        assertEquals(value1, gauge.getValue());
        metricRegistry.remove(name);
        gauge = metricRegistry.gauge(name, () -> new TestGauge(value2));
        assertEquals(value2, gauge.getValue());
    }

    @Test
    public void getMetrics() {
        com.codahale.metrics.MetricRegistry mr = MetricRegistryService.DEFAULT.createMetricRegistry(null, null, null);
        MetricRegistry metricRegistry = MetricRegistryImpl.build(mr);
        String gaugeName = buildMetricName("getMetrics_gaugeName");
        String value1 = "test_remove_value1";
        Gauge gauge = metricRegistry.gauge(gaugeName, () -> new TestGauge(value1));
        assertEquals(value1, gauge.getValue());
        try {
            Counter counter = metricRegistry.counter(gaugeName);
            assertTrue("must throw error", false);
        } catch (Exception e) {
            assertNotNull(e);
        }
        String countName = buildMetricName("getMetrics_countName");
        Counter counter = metricRegistry.counter(countName);
        counter.inc();
        String meterName = buildMetricName("getMetrics_meterName");
        Meter meter = metricRegistry.meter(meterName);
        meter.mark();
        String histogramName = buildMetricName("getMetrics_histogramName");
        Histogram histogram = metricRegistry.histogram(histogramName);
        histogram.update(10);
        String timerName = buildMetricName("getMetrics_timerName");
        Timer timer = metricRegistry.timer(timerName);
        timer.update(1, TimeUnit.MILLISECONDS);

        Map<String, Metric> metricMap = metricRegistry.getMetrics();
        assertEquals(5, metricMap.size());
        assertEquals(gauge, metricMap.get(gaugeName));
        assertEquals(counter, metricMap.get(countName));
        assertEquals(meter, metricMap.get(meterName));
        assertEquals(histogram, metricMap.get(histogramName));
        assertEquals(timer, metricMap.get(timerName));

    }

    @Test
    public void counter() {
        Counter counter = metricRegistry.counter(countName);
        counter.inc();
        checkCount(1);
        counter.inc();
        checkCount(2);
        counter.dec();
        checkCount(1);
        counter.inc(2);
        checkCount(3);
        counter.dec(3);
        checkCount(0);
        counter = metricRegistry.counter(countName + "aaa");
        counter.inc(100);
        checkCount(0);
    }

    public void checkCount(int count) {
        Counter counter = metricRegistry.counter(countName);
        assertEquals(count, counter.getCount());
    }

    @Test
    public void gauge() {
        String value = "test_gauge_value";
        String value2 = "test_gauge_value2";
        Gauge gauge = metricRegistry.gauge(gaugeName, () -> new TestGauge(value));
        assertTrue(gauge instanceof TestGauge);
        assertEquals(value, gauge.getValue());

        com.codahale.metrics.Gauge g = mr.gauge(gaugeName, () -> null);
        assertNotNull(g);
        assertEquals(value, g.getValue());

        TestGauge testGauge = (TestGauge) gauge;
        testGauge.setValue(value2);
        assertEquals(value2, g.getValue());
    }

    @Test
    public void histogram() {
        Histogram histogram = metricRegistry.histogram(histogramName);
        histogram.update(10);
        assertEquals(1, histogram.getCount());
        histogram.update(100);
        assertEquals(2, histogram.getCount());
        Snapshot snapshot = histogram.getSnapshot();
        assertEquals(2, snapshot.size());
        assertEquals(10, snapshot.getMin());
        assertEquals(100, snapshot.getMax());
    }

    @Test
    @SuppressWarnings("all")
    public void timer() throws InterruptedException {
        Timer timer = metricRegistry.timer(timerName);
        timer.update(10, TimeUnit.MILLISECONDS);
        assertEquals(1, timer.getCount());
        timer.update(200, TimeUnit.MILLISECONDS);
        assertEquals(2, timer.getCount());
        Timer.Context context = timer.time();
        Thread.sleep(50);
        context.stop();
        assertEquals(3, timer.getCount());
        Snapshot snapshot = timer.getSnapshot();

        assertEquals(3, snapshot.size());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(10), snapshot.getMin());

        timer = metricRegistry.timer(timerName);
        timer.update(10, TimeUnit.MILLISECONDS);
        timer.update(50, TimeUnit.MILLISECONDS);
        timer.update(200, TimeUnit.MILLISECONDS);

        double median = snapshot.getMedian();
        String info = "median = " + (int) median;
        Assert.assertTrue(info, median > TimeUnit.MILLISECONDS.toNanos(20));
        Assert.assertTrue(info, median < TimeUnit.MILLISECONDS.toNanos(120));
        assertEquals(TimeUnit.MILLISECONDS.toNanos(200), snapshot.getMax());
    }


    class TestGauge implements Gauge<String> {
        private String name;

        TestGauge(String name) {
            this.name = name;
        }

        public void setValue(String name) {
            this.name = name;
        }

        @Override
        public String getValue() {
            return name;
        }
    }

}
