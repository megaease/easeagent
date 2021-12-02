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


import com.codahale.metrics.Counter;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import javax.annotation.Nonnull;

public class CounterImpl implements com.megaease.easeagent.plugin.api.metric.Counter {
    private final Counter counter;

    private CounterImpl(@Nonnull Counter counter) {
        this.counter = counter;
    }

    public static com.megaease.easeagent.plugin.api.metric.Counter build(Counter counter) {
        return counter == null ? NoOpMetrics.NO_OP_COUNTER : new CounterImpl(counter);
    }

    @Override
    public void inc() {
        counter.inc();
    }

    @Override
    public void inc(long n) {
        counter.inc(n);
    }

    @Override
    public void dec() {
        counter.dec();
    }

    @Override
    public void dec(long n) {
        counter.dec(n);
    }

    @Override
    public long getCount() {
        return counter.getCount();
    }
}
