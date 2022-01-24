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

import com.codahale.metrics.Timer;
import com.megaease.easeagent.plugin.api.metric.Snapshot;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimerImpl implements com.megaease.easeagent.plugin.api.metric.Timer {
    private final Timer timer;

    private TimerImpl(Timer timer) {
        this.timer = Objects.requireNonNull(timer, "timer must not be null");
    }

    public static com.megaease.easeagent.plugin.api.metric.Timer build(Timer timer) {
        return timer == null ? NoOpMetrics.NO_OP_TIMER : new TimerImpl(timer);
    }

    @Override
    public void update(long duration, TimeUnit unit) {
        timer.update(duration, unit);
    }

    @Override
    public void update(Duration duration) {
        timer.update(duration);
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        return timer.time(event);
    }

    @Override
    public <T> T timeSupplier(Supplier<T> event) {
        return timer.timeSupplier(event);
    }

    @Override
    public void time(Runnable event) {
        timer.time(event);
    }

    @Override
    public Context time() {
        return new ContextImpl(timer.time());
    }

    @Override
    public long getCount() {
        return timer.getCount();
    }

    @Override
    public double getFifteenMinuteRate() {
        return timer.getFifteenMinuteRate();
    }

    @Override
    public double getFiveMinuteRate() {
        return timer.getFiveMinuteRate();
    }

    @Override
    public double getMeanRate() {
        return timer.getMeanRate();
    }

    @Override
    public double getOneMinuteRate() {
        return timer.getOneMinuteRate();
    }

    @Override
    public Snapshot getSnapshot() {
        return SnapshotImpl.build(timer.getSnapshot());
    }


    public static class ContextImpl implements Context {
        private final Timer.Context context;

        ContextImpl(Timer.Context context) {
            this.context = context;
        }

        @Override
        public long stop() {
            return context.stop();
        }

        @Override
        public void close() {
            context.close();
        }
    }

}
