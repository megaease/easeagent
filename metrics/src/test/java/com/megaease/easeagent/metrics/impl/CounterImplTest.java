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

import com.megaease.easeagent.plugin.api.metric.Counter;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CounterImplTest {


    public static String buildMetricName(String name) {
        return CounterImplTest.class.getName() + "##" + name;
    }

    public static Counter buildCounter(String name) {
        com.codahale.metrics.Counter unwrap = MetricRegistryMock.getCodahaleMetricRegistry().counter(buildMetricName(name));
        return CounterImpl.build(unwrap);
    }

    @Test
    public void build() {
        Counter counter = CounterImpl.build(null);
        assertTrue(counter instanceof NoOpMetrics.NoopCounter);
        counter = buildCounter("build");
        assertTrue(counter instanceof CounterImpl);
    }

    @Test
    public void inc() {
        test("inc");
    }

    private void test(String name) {
        Counter counter = buildCounter(name);
        counter.inc();
        assertEquals(1, counter.getCount());
        counter.inc();
        assertEquals(2, counter.getCount());
        counter.inc(10);
        assertEquals(12, counter.getCount());
        counter.dec();
        assertEquals(11, counter.getCount());
        counter.dec();
        assertEquals(10, counter.getCount());
        counter.dec(5);
        assertEquals(5, counter.getCount());

        assertTrue(counter.unwrap() instanceof com.codahale.metrics.Counter);
    }

    @Test
    public void inc1() {
        test("inc1");
    }

    @Test
    public void dec() {
        test("dec");
    }

    @Test
    public void dec1() {
        test("dec1");
    }

    @Test
    public void getCount() {
        test("getCount");
    }
}
