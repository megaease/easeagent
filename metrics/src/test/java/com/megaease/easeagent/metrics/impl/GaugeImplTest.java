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

import com.megaease.easeagent.plugin.api.metric.Gauge;
import org.junit.Test;

import static org.junit.Assert.*;

public class GaugeImplTest {

    @Test
    public void getG() {
        String name = "getG";
        Gauge gauge = () -> name;
        GaugeImpl gauge1 = new GaugeImpl(gauge);
        assertEquals(gauge, gauge1.getG());
        try {
            new GaugeImpl(null);
            assertTrue("must be error", false);
        } catch (Exception e) {
            assertNotNull(e);
        }
    }

    @Test
    public void getValue() {
        String name = "getG";
        Gauge gauge = () -> name;
        GaugeImpl gauge1 = new GaugeImpl(gauge);
        assertEquals(gauge, gauge1.getG());
        assertEquals(name, gauge1.getValue());
    }
}
