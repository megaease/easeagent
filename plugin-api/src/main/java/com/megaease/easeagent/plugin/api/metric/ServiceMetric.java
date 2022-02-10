/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * a base Service Metric
 */
public abstract class ServiceMetric {
    protected final MetricRegistry metricRegistry;
    protected final NameFactory nameFactory;

    public ServiceMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
        this.metricRegistry = metricRegistry;
        this.nameFactory = nameFactory;
    }

    public Meter meter(String key, MetricSubType subType) {
        return metricRegistry.meter(nameFactory.meterName(key, subType));
    }

    public Counter counter(String key, MetricSubType subType) {
        return metricRegistry.counter(nameFactory.counterName(key, subType));
    }

    public Gauge gauge(String key, MetricSubType subType, MetricSupplier<Gauge> supplier) {
        return metricRegistry.gauge(nameFactory.gaugeName(key, subType), supplier);
    }

    public Histogram histogram(String key, MetricSubType subType) {
        return metricRegistry.histogram(nameFactory.histogramName(key, subType));
    }

    public Timer timer(String key, MetricSubType subType) {
        return metricRegistry.timer(nameFactory.timerName(key, subType));
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public NameFactory getNameFactory() {
        return nameFactory;
    }
}
