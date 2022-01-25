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

import com.codahale.metrics.Clock;
import com.megaease.easeagent.plugin.api.metric.Counter;
import com.megaease.easeagent.plugin.api.metric.Histogram;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.utils.Pair;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

public class MeterImplTest {

    public static String buildMetricName(String name) {
        return MeterImplTest.class.getName() + "##" + name;
    }

    public static Pair<Meter, com.codahale.metrics.Meter> buildMeter(String name) {
        com.codahale.metrics.Meter unwrap = MetricRegistryMock.getCodahaleMetricRegistry().meter(buildMetricName(name));
        return new Pair<>(MeterImpl.build(unwrap), unwrap);
    }

    @Test
    public void build() {
        Meter meter = MeterImpl.build(null);
        assertTrue(meter instanceof NoOpMetrics.NoopMeter);
        Pair<Meter, com.codahale.metrics.Meter> pair = buildMeter("build");
        assertTrue(pair.getO1() instanceof MeterImpl);
    }


    private void test(String name) {
        Pair<Meter, com.codahale.metrics.Meter> pair = buildMeter(name);
        Meter meter = pair.getO1();
        int count = 0;
        for (int i = 0; i < 15 * 60 / 5; i++) {
            meter.mark();
            count++;
            assertEquals(count, meter.getCount());
            meter.mark(10 + i);
            count += 10 + i;
            assertEquals(count, meter.getCount());
            MetricTestUtils.nextWindow(pair.getO2());
        }
        double meanRate = meter.getMeanRate();
        double oneMeanRate = meter.getOneMinuteRate();
        double fiveMeanRate = meter.getFiveMinuteRate();
        double fifteenMeanRate = meter.getFifteenMinuteRate();
        assertTrue(String.format("meter.getMeanRate()<%s> must > 0", meanRate), meanRate > 0);
        assertTrue(String.format("meter.getOneMinuteRate()<%s> must > 0", oneMeanRate), oneMeanRate > 0);
        assertTrue(String.format("meter.getFiveMinuteRate()<%s> must > 0", fiveMeanRate), fiveMeanRate > 0);
        assertTrue(String.format("meter.getFifteenMinuteRate()<%s> must > 0", fifteenMeanRate), fifteenMeanRate > 0);
        assertTrue(meanRate > oneMeanRate);
        assertTrue(oneMeanRate > fiveMeanRate);
        assertTrue(fiveMeanRate > fifteenMeanRate);

        assertTrue(meter.unwrap() instanceof com.codahale.metrics.Meter);
    }

    @Test
    public void mark() {
        test("mark");
    }

    @Test
    public void mark1() {
        test("mark1");
    }

    @Test
    public void getCount() {
        test("getCount");
    }

    @Test
    public void getFifteenMinuteRate() {
        test("getFifteenMinuteRate");
    }

    @Test
    public void getFiveMinuteRate() {
        test("getFiveMinuteRate");
    }

    @Test
    public void getMeanRate() {
        test("getMeanRate");
    }


    @Test
    public void getOneMinuteRate() {
        test("getOneMinuteRate");
    }
}
