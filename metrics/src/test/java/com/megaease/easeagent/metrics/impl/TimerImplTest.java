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

import com.codahale.metrics.Meter;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.utils.Pair;
import org.junit.Test;

import java.time.Duration;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class TimerImplTest {

    public static String buildMetricName(String name) {
        return TimerImplTest.class.getName() + "##" + name;
    }

    public static Pair<Timer, com.codahale.metrics.Timer> buildTimer(String name) {
        com.codahale.metrics.Timer unwrap = MetricRegistryMock.getCodahaleMetricRegistry().timer(buildMetricName(name));
        return new Pair<>(TimerImpl.build(unwrap), unwrap);
    }

    @Test
    public void build() {
        Timer timer = TimerImpl.build(null);
        assertTrue(timer instanceof NoOpMetrics.NoopTimer);
        Pair<Timer, com.codahale.metrics.Timer> pair = buildTimer("build");
        assertTrue(pair.getKey() instanceof TimerImpl);
    }


    public void test(String name) {
        Pair<Timer, com.codahale.metrics.Timer> pair = buildTimer(name);

        Timer timer = pair.getKey();

        long duration = 10;
        timer.update(duration, TimeUnit.MILLISECONDS);
        assertEquals(1, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[0]);

        duration = 100;
        timer.update(duration, TimeUnit.MILLISECONDS);
        assertEquals(2, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[1]);

        duration = 101;
        timer.update(Duration.ofMillis(duration));
        assertEquals(3, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[2]);

        duration = 103;
        long now = System.nanoTime();
        MockClock mockClock = new MockClock(Arrays.asList(now, now + TimeUnit.MILLISECONDS.toNanos(duration)));
        MetricTestUtils.mockField(timer.unwrap(), "clock", mockClock, () -> {
            timer.time(() -> {
            });
        });

        assertEquals(4, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[3]);


        duration = 104;
        now = System.nanoTime();
        mockClock = new MockClock(Arrays.asList(now, now + TimeUnit.MILLISECONDS.toNanos(duration)));
        MetricTestUtils.mockField(timer.unwrap(), "clock", mockClock, () -> {
            Timer.Context context = timer.time();
            context.stop();
        });

        assertEquals(5, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[4]);

        duration = 105;
        now = System.nanoTime();
        mockClock = new MockClock(Arrays.asList(now, now + TimeUnit.MILLISECONDS.toNanos(duration)));
        MetricTestUtils.mockField(timer.unwrap(), "clock", mockClock, () -> {
            String testTimeName = "testName";
            try {
                String result = timer.time(() -> testTimeName);
                assertEquals(testTimeName, result);
            } catch (Exception e) {
                assertFalse("must not throw error", true);
            }
        });

        assertEquals(6, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[5]);

        duration = 106;
        now = System.nanoTime();
        mockClock = new MockClock(Arrays.asList(now, now + TimeUnit.MILLISECONDS.toNanos(duration)));
        MetricTestUtils.mockField(timer.unwrap(), "clock", mockClock, () -> {
            String testTimeName = "timeSupplier";
            try {
                String result = timer.timeSupplier(() -> testTimeName);
                assertEquals(testTimeName, result);
            } catch (Exception e) {
                assertFalse("must not throw error", true);
            }
        });

        assertEquals(7, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[6]);


        Snapshot snapshot = timer.getSnapshot();
        assertNotNull(snapshot);
        assertEquals(7, snapshot.size());
        assertTrue(snapshot.unwrap() instanceof com.codahale.metrics.Snapshot);


        duration = 107;
        now = System.nanoTime();
        mockClock = new MockClock(Arrays.asList(now, now + TimeUnit.MILLISECONDS.toNanos(duration)));
        MetricTestUtils.mockField(timer.unwrap(), "clock", mockClock, () -> {
            Timer.Context context = timer.time();
            context.close();
        });

        assertEquals(8, timer.getCount());
        assertEquals(TimeUnit.MILLISECONDS.toNanos(duration), timer.getSnapshot().getValues()[7]);

    }


    @Test
    public void update() {
        test("update");

    }

    @Test
    public void update1() {
        test("update1");
    }

    @Test
    public void time() {
        test("time");
    }

    @Test
    public void timeSupplier() {
        test("timeSupplier");
    }

    @Test
    public void time1() {
        test("time1");
    }

    @Test
    public void time2() {
        test("time2");
    }

    @Test
    public void getCount() {
        test("getCount");
    }

    @Test
    public void getFifteenMinuteRate() {
        testRate("getFifteenMinuteRate");
    }

    @Test
    public void getFiveMinuteRate() {
        testRate("getFiveMinuteRate");
    }

    @Test
    public void getMeanRate() {
        testRate("getMeanRate");
    }

    @Test
    public void getOneMinuteRate() {
        testRate("getOneMinuteRate");
    }

    @Test
    public void getSnapshot() {
        test("getSnapshot");
    }

    private void testRate(String name) {
        Pair<Timer, com.codahale.metrics.Timer> pair = buildTimer(name);

        Timer timer = pair.getKey();
        Meter meter = AgentFieldReflectAccessor.getFieldValue(timer.unwrap(), "meter");
        assertNotNull(meter);

        Random random = new Random();
        int count = 0;
        for (int i = 0; i < 15 * 60 / 5; i++) {
            timer.update(random.nextInt(2000), TimeUnit.MILLISECONDS);
            count++;
            assertEquals(count, timer.getCount());
            for (int j = 0; j < 10 + i; j++) {
                timer.update(random.nextInt(2000), TimeUnit.MILLISECONDS);
            }
            count += 10 + i;
            assertEquals(count, timer.getCount());
            MetricTestUtils.nextWindow(meter);
        }
        double meanRate = timer.getMeanRate();
        double oneMeanRate = timer.getOneMinuteRate();
        double fiveMeanRate = timer.getFiveMinuteRate();
        double fifteenMeanRate = timer.getFifteenMinuteRate();
        assertTrue(String.format("meter.getMeanRate()<%s> must > 0", meanRate), meanRate > 0);
        assertTrue(String.format("meter.getOneMinuteRate()<%s> must > 0", oneMeanRate), oneMeanRate > 0);
        assertTrue(String.format("meter.getFiveMinuteRate()<%s> must > 0", fiveMeanRate), fiveMeanRate > 0);
        assertTrue(String.format("meter.getFifteenMinuteRate()<%s> must > 0", fifteenMeanRate), fifteenMeanRate > 0);
        assertTrue(meanRate > oneMeanRate);
        assertTrue(oneMeanRate > fiveMeanRate);
        assertTrue(fiveMeanRate > fifteenMeanRate);

    }


}
