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

import java.util.concurrent.TimeUnit;

public class MetricRegistryImplTest {
    String countName = "countName";
    String meterName = "meterName";
    String histogramName = "histogramName";
    String timerName = "timerName";
    MetricRegistry metricRegistry = MetricRegistryImpl.build(MetricRegistryService.DEFAULT.createMetricRegistry());

    @Test
    public void build() {
        Assert.assertNotNull(metricRegistry);
    }

    @Test
    public void meter() {
        Meter meter = metricRegistry.meter(meterName);
        meter.mark();
        Assert.assertEquals(1, meter.getCount());
        System.out.println(meter.getFifteenMinuteRate() + "");
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
        Assert.assertEquals(count, counter.getCount());
    }

    @Test
    public void gauge() {
        MetricRegistry metricRegistry = MetricRegistryImpl.build(MetricRegistryService.DEFAULT.createMetricRegistry());
        String name = "test_gauge";
        String value = "test_gauge_value";
        Gauge gauge = metricRegistry.gauge(name, () -> new TestGauge("test_gauge_value"));
        Assert.assertEquals(value, gauge.getValue());
    }

    @Test
    public void histogram() {
        Histogram histogram = metricRegistry.histogram(histogramName);
        histogram.update(10);
        Assert.assertEquals(1, histogram.getCount());
        histogram.update(100);
        Assert.assertEquals(2, histogram.getCount());
        Snapshot snapshot = histogram.getSnapshot();
        Assert.assertEquals(2, snapshot.size());
        Assert.assertEquals(10, snapshot.getMin());
        Assert.assertEquals(100, snapshot.getMax());
    }

    @Test
    public void timer() throws InterruptedException {
        Timer timer = metricRegistry.timer(timerName);
        timer.update(10, TimeUnit.MILLISECONDS);
        Assert.assertEquals(1, timer.getCount());
        timer.update(100, TimeUnit.MILLISECONDS);
        Assert.assertEquals(2, timer.getCount());
        Timer.Context context = timer.time();
        Thread.sleep(15);
        context.stop();
        Assert.assertEquals(3, timer.getCount());
        Snapshot snapshot = timer.getSnapshot();
        Assert.assertEquals(3, snapshot.size());
        Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(10), snapshot.getMin());
        Assert.assertTrue(snapshot.getMedian() > TimeUnit.MILLISECONDS.toNanos(14));
        Assert.assertTrue(snapshot.getMedian() < TimeUnit.MILLISECONDS.toNanos(20));
        Assert.assertEquals(TimeUnit.MILLISECONDS.toNanos(100), snapshot.getMax());
    }

    class TestGauge implements Gauge<String> {
        private String name;

        TestGauge(String name) {
            this.name = name;
        }

        @Override
        public String getValue() {
            return name;
        }
    }
}
