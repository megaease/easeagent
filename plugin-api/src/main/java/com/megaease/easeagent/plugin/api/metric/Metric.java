package com.megaease.easeagent.plugin.api.metric;

public interface Metric {
    Meter meter(String name);

    Counter counter(String name);

    Gauge<?> gauge(String name);

    Histogram histogram(String name);

    Timer timer(String name);
}
