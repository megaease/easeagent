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

import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import org.junit.Test;

import static org.junit.Assert.*;

public class MetricInstanceTest {

    @Test
    public void to() {
        Counter counter = CounterImpl.build(null);
        String testErrorName = "testErrorName";
        counter = MetricInstance.COUNTER.to(testErrorName, counter);
        assertNotNull(counter);

        Histogram histogram = MetricInstance.HISTOGRAM.to(testErrorName, HistogramImpl.build(null));
        assertNotNull(histogram);
        try {
            MetricInstance.HISTOGRAM.to(testErrorName, counter);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        Meter meter = MetricInstance.METER.to(testErrorName, MeterImpl.build(null));
        assertNotNull(meter);
        try {
            MetricInstance.METER.to(testErrorName, counter);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        Timer timer = MetricInstance.TIMER.to(testErrorName, TimerImpl.build(null));
        assertNotNull(timer);
        try {
            MetricInstance.TIMER.to(testErrorName, counter);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        Gauge gauge = MetricInstance.GAUGE.to(testErrorName, NoOpMetrics.NO_OP_GAUGE);
        assertNotNull(gauge);

        try {
            MetricInstance.GAUGE.to(testErrorName, counter);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }

        try {
            MetricInstance.COUNTER.to(testErrorName, gauge);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void toInstance() {
        to();
    }
}
