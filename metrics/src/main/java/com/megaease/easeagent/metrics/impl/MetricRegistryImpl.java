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

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.utils.NoNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Supplier;

public class MetricRegistryImpl implements com.megaease.easeagent.plugin.api.metric.MetricRegistry {
    private final ConcurrentMap<String, Gauge> gauges;
    private final MetricRegistry metricRegistry;

    private MetricRegistryImpl(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.gauges = new ConcurrentHashMap<>();
    }

    public static com.megaease.easeagent.plugin.api.metric.MetricRegistry build(MetricRegistry metricRegistry) {
        return metricRegistry == null ? NoOpMetrics.NO_OP_METRIC : new MetricRegistryImpl(metricRegistry);
    }


    @Override
    public boolean remove(String name) {
        gauges.remove(name);
        return metricRegistry.remove(name);
    }

    @Override
    public Map<String, Metric> getMetrics() {
        Map<String, com.codahale.metrics.Metric> metricMap = metricRegistry.getMetrics();
        Map<String, Metric> result = new HashMap<>();
        for (Map.Entry<String, com.codahale.metrics.Metric> entry : metricMap.entrySet()) {
            com.codahale.metrics.Metric oleMetric = entry.getValue();
            if (oleMetric instanceof com.codahale.metrics.Meter) {
                result.put(entry.getKey(), MeterImpl.build((com.codahale.metrics.Meter) oleMetric));
            } else if (oleMetric instanceof com.codahale.metrics.Counter) {
                result.put(entry.getKey(), CounterImpl.build((com.codahale.metrics.Counter) oleMetric));
            } else if (oleMetric instanceof com.codahale.metrics.Histogram) {
                result.put(entry.getKey(), HistogramImpl.build((com.codahale.metrics.Histogram) oleMetric));
            } else if (oleMetric instanceof com.codahale.metrics.Timer) {
                result.put(entry.getKey(), TimerImpl.build((com.codahale.metrics.Timer) oleMetric));
            }
        }
        result.putAll(this.gauges);
        return Collections.unmodifiableMap(result);
    }

    @Override
    public Meter meter(String name) {
        return NoNull.of(MeterImpl.build(metricRegistry.meter(name)), NoOpMetrics.NO_OP_METER);
    }

    @Override
    public Counter counter(String name) {
        return NoNull.of(CounterImpl.build(metricRegistry.counter(name)), NoOpMetrics.NO_OP_COUNTER);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Gauge gauge(String name, Supplier<Gauge> supplier) {
        Gauge gauge = gauges.get(name);
        if (gauge != null) {
            return gauge;
        }
        com.codahale.metrics.Gauge result = metricRegistry.gauge(name, new GaugeSupplier(supplier));
        gauge = ((GaugeImpl) result).getG();
        gauges.put(name, gauge);
        return gauge;
    }

    @Override
    public Histogram histogram(String name) {
        return NoNull.of(HistogramImpl.build(metricRegistry.histogram(name)), NoOpMetrics.NO_OP_HISTOGRAM);
    }

    @Override
    public Timer timer(String name) {
        return NoNull.of(TimerImpl.build(metricRegistry.timer(name)), NoOpMetrics.NO_OP_TIMER);
    }

    public static class GaugeSupplier implements MetricRegistry.MetricSupplier<com.codahale.metrics.Gauge> {
        private final Supplier<Gauge> supplier;

        GaugeSupplier(@Nonnull Supplier<Gauge> supplier) {
            this.supplier = supplier;
        }

        @Override
        public com.codahale.metrics.Gauge newMetric() {
            Gauge newGauge = supplier.get();
            return new GaugeImpl(newGauge);
        }
    }
}
