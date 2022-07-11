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

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricRegistryListener;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.utils.NoNull;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MetricRegistryImpl implements com.megaease.easeagent.plugin.api.metric.MetricRegistry {
    private final ConcurrentMap<String, Metric> metricCache;
    private final MetricRegistry metricRegistry;

    MetricBuilder<Counter> counters = new MetricBuilder<Counter>() {
        @Override
        public Counter newMetric(String name) {
            return NoNull.of(CounterImpl.build(metricRegistry.counter(name)), NoOpMetrics.NO_OP_COUNTER);
        }
    };

    MetricBuilder<Histogram> histograms = new MetricBuilder<Histogram>() {
        @Override
        public Histogram newMetric(String name) {
            return NoNull.of(HistogramImpl.build(metricRegistry.histogram(name)), NoOpMetrics.NO_OP_HISTOGRAM);
        }

    };

    MetricBuilder<Meter> meters = new MetricBuilder<Meter>() {
        @Override
        public Meter newMetric(String name) {
            return NoNull.of(MeterImpl.build(metricRegistry.meter(name)), NoOpMetrics.NO_OP_METER);
        }

    };

    MetricBuilder<Timer> timers = new MetricBuilder<Timer>() {
        @Override
        public Timer newMetric(String name) {
            return NoNull.of(TimerImpl.build(metricRegistry.timer(name)), NoOpMetrics.NO_OP_TIMER);
        }
    };

    private MetricRegistryImpl(MetricRegistry metricRegistry) {
        this.metricRegistry = Objects.requireNonNull(metricRegistry, "metricRegistry must not be null");
        this.metricCache = new ConcurrentHashMap<>();
        this.metricRegistry.addListener(new MetricRemoveListener());

    }

    public static com.megaease.easeagent.plugin.api.metric.MetricRegistry build(MetricRegistry metricRegistry) {
        return metricRegistry == null ? NoOpMetrics.NO_OP_METRIC : new MetricRegistryImpl(metricRegistry);
    }


    @Override
    public boolean remove(String name) {
        synchronized (metricCache) {
            return metricRegistry.remove(name);
        }
    }

    private <T extends Metric> T getOrAdd(String name, MetricInstance<T> instance, MetricBuilder<T> builder) {
        Metric metric = metricCache.get(name);
        if (metric != null) {
            return instance.to(name, metric);
        }
        synchronized (metricCache) {
            metric = metricCache.get(name);
            if (metric != null) {
                return instance.to(name, metric);
            }
            T t = builder.newMetric(name);
            metricCache.putIfAbsent(name, t);
            return t;
        }
    }

    @Override
    public Map<String, Metric> getMetrics() {
        return Collections.unmodifiableMap(metricCache);
    }

    @Override
    public Meter meter(String name) {
        return getOrAdd(name, MetricInstance.METER, meters);
    }

    @Override
    public Counter counter(String name) {
        return getOrAdd(name, MetricInstance.COUNTER, counters);
    }


    @Override
    @SuppressWarnings("rawtypes")
    public Gauge gauge(String name, MetricSupplier<Gauge> supplier) {
        Metric metric = metricCache.get(name);
        if (metric != null) {
            return MetricInstance.GAUGE.to(name, metric);
        }
        synchronized (metricCache) {
            metric = metricCache.get(name);
            if (metric != null) {
                return MetricInstance.GAUGE.to(name, metric);
            }
            com.codahale.metrics.Gauge result = metricRegistry.gauge(name, new GaugeSupplier(supplier));
            Gauge g = ((GaugeImpl) result).getG();
            metricCache.putIfAbsent(name, g);
            return g;
        }
    }

    @Override
    public Histogram histogram(String name) {
        return getOrAdd(name, MetricInstance.HISTOGRAM, histograms);
    }

    @Override
    public Timer timer(String name) {
        return getOrAdd(name, MetricInstance.TIMER, timers);
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public static class GaugeSupplier implements MetricRegistry.MetricSupplier<com.codahale.metrics.Gauge> {
        private final MetricSupplier<Gauge> supplier;

        GaugeSupplier(@Nonnull MetricSupplier<Gauge> supplier) {
            this.supplier = supplier;
        }

        @Override
        public com.codahale.metrics.Gauge newMetric() {
            Gauge newGauge = supplier.newMetric();
            return new GaugeImpl(newGauge);
        }
    }

    /**
     * A quick and easy way of capturing the notion of default metrics.
     */
    private interface MetricBuilder<T extends Metric> {
        T newMetric(String name);
    }

    class MetricRemoveListener implements MetricRegistryListener {

        /**
         * Do nothing because of added by {@link MetricRegistryImpl#getOrAdd(String, MetricInstance, MetricBuilder)}
         * @param name
         * @param gauge
         */
        @Override
        public void onGaugeAdded(String name, com.codahale.metrics.Gauge<?> gauge) {
            //Do nothing
        }

        @Override
        public void onGaugeRemoved(String name) {
            synchronized (metricCache) {
                metricCache.remove(name);
            }
        }

        /**
         * Do nothing because of added by {@link MetricRegistryImpl#getOrAdd(String, MetricInstance, MetricBuilder)}
         * @param name
         * @param counter
         */
        @Override
        public void onCounterAdded(String name, com.codahale.metrics.Counter counter) {
            //Do nothing
        }

        @Override
        public void onCounterRemoved(String name) {
            synchronized (metricCache) {
                metricCache.remove(name);
            }
        }

        /**
         * Do nothing because of added by {@link MetricRegistryImpl#getOrAdd(String, MetricInstance, MetricBuilder)}
         * @param name
         * @param histogram
         */
        @Override
        public void onHistogramAdded(String name, com.codahale.metrics.Histogram histogram) {
            //Do nothing
        }

        @Override
        public void onHistogramRemoved(String name) {
            synchronized (metricCache) {
                metricCache.remove(name);
            }

        }

        /**
         * Do nothing because of added by {@link MetricRegistryImpl#getOrAdd(String, MetricInstance, MetricBuilder)}
         * @param name
         * @param meter
         */
        @Override
        public void onMeterAdded(String name, com.codahale.metrics.Meter meter) {
            //Do nothing
        }

        @Override
        public void onMeterRemoved(String name) {
            synchronized (metricCache) {
                metricCache.remove(name);
            }

        }

        /**
         * Do nothing because of added by {@link MetricRegistryImpl#getOrAdd(String, MetricInstance, MetricBuilder)}
         *
         * @param name
         * @param timer
         */
        @Override
        public void onTimerAdded(String name, com.codahale.metrics.Timer timer) {
            //Do nothing
        }

        @Override
        public void onTimerRemoved(String name) {
            synchronized (metricCache) {
                metricCache.remove(name);
            }
        }
    }

}
