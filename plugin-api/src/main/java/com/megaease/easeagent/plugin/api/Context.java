package com.megaease.easeagent.plugin.api;

import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.trace.Tracer;

public interface Context {
    Tracer currentTrace();

    Metric getMetric();
}
