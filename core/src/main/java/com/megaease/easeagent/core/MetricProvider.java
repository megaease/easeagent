package com.megaease.easeagent.core;

import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;

public interface MetricProvider {
    MetricRegistrySupplier metricSupplier();
}
