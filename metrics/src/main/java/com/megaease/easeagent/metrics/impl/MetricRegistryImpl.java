package com.megaease.easeagent.metrics.impl;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.utils.NoNull;

import javax.annotation.Nonnull;
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
    public Meter meter(String name) {
        return NoNull.of(MeterImpl.build(metricRegistry.meter(name)), NoOpMetrics.NO_OP_METER);
    }

    @Override
    public Counter counter(String name) {
        return NoNull.of(CounterImpl.build(metricRegistry.counter(name)), NoOpMetrics.NO_OP_COUNTER);
    }

    @Override
    public <T> Gauge<T> gauge(String name, Supplier<Gauge<T>> supplier) {
        Gauge gauge = gauges.get(name);
        if (gauge != null) {
            return gauge;
        }
        com.codahale.metrics.Gauge<Gauge<T>> result = metricRegistry.gauge(name, new GaugeSupplier(supplier));
        gauge = result.getValue();
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

    public static class GaugeSupplier<T> implements MetricRegistry.MetricSupplier<com.codahale.metrics.Gauge<T>> {
        private final Supplier<Gauge<T>> supplier;

        public GaugeSupplier(@Nonnull Supplier<Gauge<T>> supplier) {
            this.supplier = supplier;
        }

        @Override
        public com.codahale.metrics.Gauge newMetric() {
            Gauge<T> newGauge = supplier.get();
            return new GaugeImpl(newGauge);
        }
    }
}
