package com.hexdecteam.easeagent;

import com.codahale.metrics.*;
import com.google.auto.service.AutoService;
import org.stagemonitor.core.metrics.metrics2.Metric2Registry;
import org.stagemonitor.core.metrics.metrics2.MetricName;

import java.util.Map;
import java.util.concurrent.Callable;

import static org.stagemonitor.core.metrics.metrics2.MetricName.name;

@AutoService(Metrics.class)
public class MetricsX implements Metrics {
    private final Metric2Registry registry = new Metric2Registry();

    @Override
    public void iterate(Consumer consumer) {
        final Map<MetricName, Metric> metrics = registry.getMetrics();
        for (Map.Entry<MetricName, Metric> entry : metrics.entrySet()) {
            final MetricName name = entry.getKey();
            consumer.accept(name.getName(), name.getTags(), entry.getValue());
        }
    }

    @Override
    public Counter counter(String name, Map<String, String> tags) {
        return registry.counter(name(name).tags(tags).build());
    }

    @Override
    public Meter meter(String name, Map<String, String> tags) {
        return registry.meterExt(name(name).tags(tags).build());
    }

    @Override
    public Timer timer(String name, Map<String, String> tags) {
        return registry.timerExt(name(name).tags(tags).build());
    }

    @Override
    public void registerIfAbsent(String name, Callable<Gauge<Object>> supplier) throws Exception {
        final MetricName mn = name(name).build();
        if (registry.getNames().contains(mn)) return;
        registry.register(mn, supplier.call());
    }
}
