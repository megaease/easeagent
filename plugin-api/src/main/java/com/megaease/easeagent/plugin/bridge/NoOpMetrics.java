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

package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;

import java.io.OutputStream;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.megaease.easeagent.plugin.bridge.NoOpReporter.NO_OP_REPORTER;

public final class NoOpMetrics {
    public static final MetricRegistrySupplier NO_OP_METRIC_SUPPLIER = NoopMetricsRegistrySupplier.INSTANCE;
    public static final Gauge NO_OP_GAUGE = NoopGauge.INSTANCE;
    public static final Snapshot NO_OP_SNAPSHOT = NoopSnapshot.INSTANCE;
    public static final Timer NO_OP_TIMER = NoopTimer.INSTANCE;
    public static final Histogram NO_OP_HISTOGRAM = NoopHistogram.INSTANCE;
    public static final Counter NO_OP_COUNTER = NoopCounter.INSTANCE;
    public static final Meter NO_OP_METER = NoopMeter.INSTANCE;
    public static final MetricRegistry NO_OP_METRIC = NoopMetricRegistry.INSTANCE;

    public static final class NoopMetricsRegistrySupplier implements MetricRegistrySupplier {
        private static final NoopMetricsRegistrySupplier INSTANCE = new NoopMetricsRegistrySupplier();

        @Override
        public MetricRegistry newMetricRegistry(Config config, NameFactory nameFactory, Tags tags) {
            return NoopMetricRegistry.INSTANCE;
        }

        @Override
        public Reporter reporter(Config config) {
            return NO_OP_REPORTER;
        }
    }

    public static final class NoopGauge<T> implements Gauge<T> {
        private static final NoopGauge INSTANCE = new NoopGauge<>();

        /**
         * {@inheritDoc}
         */
        @Override
        public T getValue() {
            return null;
        }
    }

    public static final class NoopSnapshot implements Snapshot {
        private static final NoopSnapshot INSTANCE = new NoopSnapshot();
        private static final long[] EMPTY_LONG_ARRAY = new long[0];

        /**
         * {@inheritDoc}
         */
        @Override
        public double getValue(double quantile) {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long[] getValues() {
            return EMPTY_LONG_ARRAY;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getMax() {
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getMean() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getMin() {
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getStdDev() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dump(OutputStream output) {
            // NOP
        }
    }

    public static final class NoopTimer implements Timer {
        private static final NoopTimer INSTANCE = new NoopTimer();
        private static final Timer.Context CONTEXT = new NoopTimer.Context();

        private static class Context implements Timer.Context {

            /**
             * {@inheritDoc}
             */
            @Override
            public long stop() {
                return 0L;
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public void close() {
                // NOP
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(long duration, TimeUnit unit) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(Duration duration) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T time(Callable<T> event) throws Exception {
            return event.call();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public <T> T timeSupplier(Supplier<T> event) {
            return event.get();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void time(Runnable event) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Timer.Context time() {
            return CONTEXT;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getCount() {
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getFifteenMinuteRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getFiveMinuteRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getMeanRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getOneMinuteRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Snapshot getSnapshot() {
            return NoopSnapshot.INSTANCE;
        }
    }

    public static final class NoopHistogram implements Histogram {
        private static final NoopHistogram INSTANCE = new NoopHistogram();

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(int value) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void update(long value) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getCount() {
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Snapshot getSnapshot() {
            return NoopSnapshot.INSTANCE;
        }
    }

    public static final class NoopCounter implements Counter {
        private static final NoopCounter INSTANCE = new NoopCounter();

        /**
         * {@inheritDoc}
         */
        @Override
        public void inc() {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inc(long n) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dec() {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dec(long n) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getCount() {
            return 0L;
        }
    }

    public static final class NoopMeter implements Meter {
        private static final NoopMeter INSTANCE = new NoopMeter();

        /**
         * {@inheritDoc}
         */
        @Override
        public void mark() {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void mark(long n) {
            // NOP
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getCount() {
            return 0L;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getFifteenMinuteRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getFiveMinuteRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getMeanRate() {
            return 0D;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public double getOneMinuteRate() {
            return 0D;
        }
    }

    public static final class NoopMetricRegistry implements MetricRegistry {
        private static final NoopMetricRegistry INSTANCE = new NoopMetricRegistry();

        @Override
        public boolean remove(String name) {
            return true;
        }

        @Override
        public Meter meter(String name) {
            return NoopMeter.INSTANCE;
        }

        @Override
        public Counter counter(String name) {
            return NoopCounter.INSTANCE;
        }

        @Override
        public Gauge gauge(String name, Supplier<Gauge> supplier) {
            return NoopGauge.INSTANCE;
        }

        @Override
        public Histogram histogram(String name) {
            return NoopHistogram.INSTANCE;
        }

        @Override
        public Timer timer(String name) {
            return NoopTimer.INSTANCE;
        }
    }


}
