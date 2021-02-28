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

package com.megaease.easeagent.metrics;

import com.codahale.metrics.Counting;
import com.codahale.metrics.Metered;
import com.codahale.metrics.Snapshot;

import java.util.function.Function;

public enum MetricValueFetcher {
    CountingCount(Counting::getCount, Counting.class),
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
    MeteredM1Rate(Metered::getOneMinuteRate, Metered.class),
    MeteredM1RateIgnoreZero(Metered::getOneMinuteRate, Metered.class, aDouble -> {
        if (aDouble.compareTo(0.00001) < 0) {
            throw new IllegalArgumentException("current value is too small: " + aDouble);
        }
        return aDouble;
    }),
    MeteredM5Rate(Metered::getFiveMinuteRate, Metered.class),
    MeteredM15Rate(Metered::getFifteenMinuteRate, Metered.class),
    MeteredMeanRate(Metered::getMeanRate, Metered.class),
    MeteredCount(Metered::getCount, Metered.class);

    public static <T, V> Function<T, V> wrapIgnoreZeroFunc(Function<T, V> origin) {
        return null;
    }

    Function func;
    Class clazz;
    Function checker;

    <T, V> MetricValueFetcher(Function<T, V> function, Class<T> clazz) {
        this(function, clazz, v -> v);
    }

    <T, V> MetricValueFetcher(Function<T, V> function, Class<T> clazz, Function<V, V> checker) {
        this.func = function;
        this.clazz = clazz;
        this.checker = checker;
    }

    Object apply(Object obj) {
        return checker.apply(func.apply(clazz.cast(obj)));
    }
}
