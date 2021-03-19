package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.metrics.converter.Converter;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AutoRefreshReporter implements Runnable {
    private final MetricsCollectorConfig config;
    private final Converter converter;
    private final Consumer<String> consumer;
    private final MetricRegistry metricRegistry;
    private AgentScheduledReporter reporter;

    public AutoRefreshReporter(MetricRegistry metricRegistry, MetricsCollectorConfig config, Converter converter, Consumer<String> consumer) {
        this.metricRegistry = metricRegistry;
        this.config = config;
        this.consumer = consumer;
        this.converter = converter;
        config.setIntervalChangeCallback(this);
    }

    @Override
    public synchronized void run() {
        if (reporter != null) {
            reporter.close();
            reporter = null;
        }
        reporter = AgentScheduledReporter.forRegistry(metricRegistry)
                .outputTo(consumer)
                .enabled(config::isEnabled)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        reporter.setConverter(converter);
        reporter.start(config.getInterval(), TimeUnit.SECONDS);
    }
}