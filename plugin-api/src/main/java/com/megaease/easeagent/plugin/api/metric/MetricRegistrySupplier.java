package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;

public interface MetricRegistrySupplier {
    MetricRegistry newMetricRegistry(Config config, NameFactory nameFactory, Tags tags);
}
