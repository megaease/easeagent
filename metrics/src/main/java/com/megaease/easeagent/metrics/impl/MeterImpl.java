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

import com.codahale.metrics.Meter;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import java.util.Objects;

public class MeterImpl implements com.megaease.easeagent.plugin.api.metric.Meter {
    private final Meter meter;

    private MeterImpl(Meter meter) {
        this.meter = Objects.requireNonNull(meter, "meter must not be null");
    }

    public static com.megaease.easeagent.plugin.api.metric.Meter build(Meter meter) {
        return meter == null ? NoOpMetrics.NO_OP_METER : new MeterImpl(meter);
    }

    @Override
    public void mark() {
        meter.mark();
    }

    @Override
    public void mark(long n) {
        meter.mark(n);
    }

    @Override
    public long getCount() {
        return meter.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return meter.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return meter.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return meter.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return meter.getOneMinuteRate();
    }
}
