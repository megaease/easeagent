package com.megaease.easeagent;

import com.codahale.metrics.*;
import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;

@AutoService(Metrics.class)
public class StdMetrics implements Metrics {
    private static final Map<String, String> EMPTY = Collections.emptyMap();

    private final MetricRegistry registry = new MetricRegistry();

    @Override
    public void iterate(Consumer consumer) {
        final Map<String, Metric> metrics = registry.getMetrics();
        for (Map.Entry<String, Metric> entry : metrics.entrySet()) {
            consumer.accept(entry.getKey(), EMPTY, entry.getValue());
        }
    }

    @Override
    public Counter counter(String name, Map<String, String> tags) {
        return registry.counter(name(name, tags));
    }

    @Override
    public Meter meter(String name, Map<String, String> tags) {
        return registry.meter(name(name, tags));
    }

    @Override
    public Timer timer(String name, Map<String, String> tags) {
        return registry.timer(name(name, tags));
    }

    @Override
    public void registerIfAbsent(String name, Callable<Gauge<Object>> supplier) throws Exception {
        if (registry.getNames().contains(name)) return;
        registry.register(name, supplier.call());
    }

    private String name(String name, Map<String, String> tags) {
        if (tags.isEmpty()) return name;
        return name + ':' + Joiner.on(',').withKeyValueSeparator('=').join(tags);
    }
}
