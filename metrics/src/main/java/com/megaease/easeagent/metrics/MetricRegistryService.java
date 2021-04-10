package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;

import java.util.ArrayList;
import java.util.List;

public class MetricRegistryService {

    public static final MetricRegistryService DEFAULT = new MetricRegistryService();

    private static final List<MetricRegistry> REGISTRY_LIST = new ArrayList<>();

    public MetricRegistry createMetricRegistry() {
        MetricRegistry registry = new MetricRegistry();
        REGISTRY_LIST.add(registry);
        new DropwizardExports(registry).register();
        return registry;
    }
}
