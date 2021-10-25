package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.config.Config;

public interface MetricSupplier {
    Metric newMetric(Config config);
}
