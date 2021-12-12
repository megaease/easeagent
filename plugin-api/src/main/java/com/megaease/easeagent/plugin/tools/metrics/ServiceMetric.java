package com.megaease.easeagent.plugin.tools.metrics;

import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;

import javax.annotation.Nonnull;

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
}
