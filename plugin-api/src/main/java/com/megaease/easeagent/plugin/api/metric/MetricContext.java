package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.Context;

public interface MetricContext extends Context {
    void setMetric(Metric metric);
}
