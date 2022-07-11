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

package com.megaease.easeagent.metrics.impl;

import com.megaease.easeagent.plugin.api.metric.Histogram;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HistogramImplTest {

    public static String buildMetricName(String name) {
        return HistogramImplTest.class.getName() + "##" + name;
    }

    public static Histogram buildhistogram(String name) {
        com.codahale.metrics.Histogram unwrap = MetricRegistryMock.getCodahaleMetricRegistry().histogram(buildMetricName(name));
        return HistogramImpl.build(unwrap);
    }

    @Test
    public void build() {
        Histogram histogram = HistogramImpl.build(null);
        assertTrue(histogram instanceof NoOpMetrics.NoopHistogram);
        histogram = buildhistogram("build");
        assertTrue(histogram instanceof HistogramImpl);
    }

    @Test
    public void update() {
        test("update");
    }

    private void test(String name) {
        Histogram histogram = buildhistogram(name);
        histogram.update(1);
        histogram.update(1);
        assertEquals(2, histogram.getCount());
        long value = 123456789019l;
        histogram.update(value);
        assertEquals(3, histogram.getCount());
        Snapshot snapshot = histogram.getSnapshot();
        assertEquals(3, snapshot.size());
        histogram.update(1);
        assertEquals(4, histogram.getCount());
        assertEquals(3, snapshot.size());
        assertTrue(histogram.unwrap() instanceof com.codahale.metrics.Histogram);
    }

    @Test
    public void update1() {
        test("update1");
    }

    @Test
    public void getCount() {
        test("getCount");
    }

    @Test
    public void getSnapshot() {
        test("getSnapshot");
    }
}
