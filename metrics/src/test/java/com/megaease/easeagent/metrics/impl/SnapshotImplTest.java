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

package com.megaease.easeagent.metrics.impl;

import com.megaease.easeagent.plugin.api.metric.Histogram;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.junit.Assert.*;

public class SnapshotImplTest {

    public static String buildMetricName(String name) {
        return SnapshotImplTest.class.getName() + "##" + name;
    }

    public static Histogram buildhistogram(String name) {
        com.codahale.metrics.Histogram unwrap = MetricRegistryMock.getCodahaleMetricRegistry().histogram(buildMetricName(name));
        return HistogramImpl.build(unwrap);
    }

    @Test
    public void build() {
        Snapshot snapshot = SnapshotImpl.build(null);
        assertTrue(snapshot instanceof NoOpMetrics.NoopSnapshot);
        Histogram histogram = buildhistogram("build");
        histogram.update(10);
        snapshot = histogram.getSnapshot();
        assertTrue(snapshot instanceof SnapshotImpl);
    }

    public void test(String name) {
        Histogram histogram = buildhistogram(name);
        for (int i = 0; i < 100; i++) {
            histogram.update(i);
        }
        Snapshot snapshot = histogram.getSnapshot();
        assertEquals(1, snapshot.getValue(0.01), 0.1);

        long[] values = snapshot.getValues();
        for (int i = 0; i < values.length; i++) {
            assertEquals(i, values[i]);
        }

        assertEquals(100, snapshot.size());
        assertEquals(0, snapshot.getMin());
        assertEquals(49.5, snapshot.getMean(), 0.2);
        assertEquals(99, snapshot.getMax());

        assertEquals(49, snapshot.getMedian(), 0.1);
        assertEquals(74, snapshot.get75thPercentile(), 0.1);
        assertEquals(94, snapshot.get95thPercentile(), 0.1);
        assertEquals(97, snapshot.get98thPercentile(), 0.1);
        assertEquals(98, snapshot.get99thPercentile(), 0.1);
        assertEquals(99, snapshot.get999thPercentile(), 0.1);
    }

    @Test
    public void getValue() {
        test("getValue");
    }

    @Test
    public void getValues() {
        test("getValues");
    }

    @Test
    public void size() {
        test("size");
    }

    @Test
    public void getMax() {
        test("getMax");
    }

    @Test
    public void getMean() {
        test("getMean");
    }

    @Test
    public void getMin() {
        test("getMin");
    }

    @Test
    public void getStdDev() {
        Histogram histogram = buildhistogram("getStdDev");
        //9、2、5、4、12、7
        histogram.update(9);
        histogram.update(2);
        histogram.update(5);
        histogram.update(4);
        histogram.update(12);
        histogram.update(7);
        Snapshot snapshot = histogram.getSnapshot();
        assertEquals(3.3, snapshot.getStdDev(), 0.1);
    }

    @Test
    public void dump() {
        Histogram histogram = buildhistogram("dump");
        histogram.update(10);
        Snapshot snapshot = histogram.getSnapshot();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        snapshot.dump(byteArrayOutputStream);
        assertEquals("10", byteArrayOutputStream.toString().trim());
    }

    @Test
    public void unwrap() {
        Histogram histogram = buildhistogram("unwrap");
        Snapshot snapshot = histogram.getSnapshot();
        assertTrue(snapshot.unwrap() instanceof com.codahale.metrics.Snapshot);
    }
}
