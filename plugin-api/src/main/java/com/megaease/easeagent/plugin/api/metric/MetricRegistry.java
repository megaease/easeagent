package com.megaease.easeagent.plugin.api.metric;

import java.util.function.Supplier;

public interface MetricRegistry {
    Meter meter(String name);

    Counter counter(String name);

    <T> Gauge<T> gauge(String name, Supplier<Gauge<T>> supplier);

    Histogram histogram(String name);

    Timer timer(String name);
}
