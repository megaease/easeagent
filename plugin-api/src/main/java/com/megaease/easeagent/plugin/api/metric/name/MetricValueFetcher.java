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

package com.megaease.easeagent.plugin.api.metric.name;

import com.megaease.easeagent.plugin.api.metric.Counter;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.Snapshot;

import java.util.function.Function;

public enum MetricValueFetcher {
    CountingCount(Counter::getCount, Counter.class),
    SnapshotMaxValue(Snapshot::getMax, Snapshot.class),
    SnapshotMeanValue(Snapshot::getMean, Snapshot.class),
    SnapshotMinValue(Snapshot::getMin, Snapshot.class),
    Snapshot25Percentile(s -> s.getValue(0.25), Snapshot.class),
    SnapshotMedianValue(Snapshot::getMedian, Snapshot.class),
    Snapshot50PercentileValue(Snapshot::getMedian, Snapshot.class),
    Snapshot75PercentileValue(Snapshot::get75thPercentile, Snapshot.class),
    Snapshot95PercentileValue(Snapshot::get95thPercentile, Snapshot.class),
    Snapshot98PercentileValue(Snapshot::get98thPercentile, Snapshot.class),
    Snapshot99PercentileValue(Snapshot::get99thPercentile, Snapshot.class),
    Snapshot999PercentileValue(Snapshot::get999thPercentile, Snapshot.class),
    MeteredM1Rate(Meter::getOneMinuteRate, Meter.class),
    MeteredM1RateIgnoreZero(Meter::getOneMinuteRate, Meter.class, aDouble -> aDouble),
    MeteredM5Rate(Meter::getFiveMinuteRate, Meter.class),
    MeteredM15Rate(Meter::getFifteenMinuteRate, Meter.class),
    MeteredMeanRate(Meter::getMeanRate, Meter.class),
    MeteredCount(Meter::getCount, Meter.class);

    public static <T, V> Function<T, V> wrapIgnoreZeroFunc(Function<T, V> origin) {
        return null;
    }

    private final Function func;
    private final Class clazz;
    private final Function checker;

    <T, V> MetricValueFetcher(Function<T, V> function, Class<T> clazz) {
        this(function, clazz, v -> v);
    }

    <T, V> MetricValueFetcher(Function<T, V> function, Class<T> clazz, Function<V, V> checker) {
        this.func = function;
        this.clazz = clazz;
        this.checker = checker;
    }

    public Function getFunc() {
        return func;
    }

    public Class getClazz() {
        return clazz;
    }

    public Function getChecker() {
        return checker;
    }

    public Object apply(Metric obj) {
        return checker.apply(func.apply(clazz.cast(obj)));
    }
}
