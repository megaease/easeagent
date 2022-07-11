/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.metrics.impl;

import com.megaease.easeagent.plugin.api.metric.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class MetricInstance<T extends Metric> {
    public static final MetricInstance<Counter> COUNTER = new MetricInstance<Counter>() {
        @Override
        protected Counter toInstance(String name, Metric metric) {
            return (Counter) metric;
        }
    };

    public static final MetricInstance<Histogram> HISTOGRAM = new MetricInstance<Histogram>() {
        @Override
        protected Histogram toInstance(String name, Metric metric) {
            return (Histogram) metric;
        }
    };

    public static final MetricInstance<Meter> METER = new MetricInstance<Meter>() {
        @Override
        protected Meter toInstance(String name, Metric metric) {
            return (Meter) metric;
        }
    };

    public static final MetricInstance<Timer> TIMER = new MetricInstance<Timer>() {
        @Override
        protected Timer toInstance(String name, Metric metric) {
            return (Timer) metric;
        }
    };

    public static final MetricInstance<Gauge> GAUGE = new MetricInstance<Gauge>() {
        @Override
        protected Gauge toInstance(String name, Metric metric) {
            return (Gauge) metric;
        }
    };

    private final Class<?> type;

    private MetricInstance() {
        Type superClass = getClass().getGenericSuperclass();
        if (superClass instanceof Class<?>) { // sanity check, should never happen
            throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
        }
        Type t = ((ParameterizedType) superClass).getActualTypeArguments()[0];
        if (!(t instanceof Class)) {
            throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
        }
        type = (Class<?>) t;
    }

    protected T to(String name, Metric metric) {
        if (!type.isInstance(metric)) {
            throw new IllegalArgumentException(String.format("%s is already used for a different type<%s> of metric", name, metric.getClass().getName()));
        }
        return toInstance(name, metric);
    }

    protected abstract T toInstance(String name, Metric metric);
}
