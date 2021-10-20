package com.megaease.easeagent.core.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricContext;
import com.megaease.easeagent.plugin.api.trace.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Tracer;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;

public class SessionContext implements Context, TraceContext, MetricContext {
    private Tracer tracer = NoOpTracer.NO_OP_TRACER;
    private Metric metric = NoOpMetrics.NO_OP_METRIC;

    @Override
    public Tracer currentTrace() {
        if (tracer == null) {
            return NoOpTracer.NO_OP_TRACER;
        }
        return tracer;
    }

    @Override
    public Metric getMetric() {
        if (metric == null) {
            return NoOpMetrics.NO_OP_METRIC;
        }
        return metric;
    }

    @Override
    public void setMetric(Metric metric) {
        this.metric = metric;
    }

    @Override
    public void setCurrentTrace(Tracer tracer) {
        this.tracer = tracer;
    }
}
