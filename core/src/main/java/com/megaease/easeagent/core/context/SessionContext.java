package com.megaease.easeagent.core.context;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;
import com.megaease.easeagent.plugin.bridge.NoOpMetrics;
import com.megaease.easeagent.plugin.bridge.NoOpTracer;
import com.megaease.easeagent.plugin.utils.NoNull;

import java.util.HashMap;
import java.util.Map;


public class SessionContext implements Context, TraceContext, MetricContext {
    private Tracing tracing = NoOpTracer.NO_OP_TRACING;
    private Metric metric = NoOpMetrics.NO_OP_METRIC;
    private Map<Object, Object> context = new HashMap<>();

    @Override
    public boolean isNoop() {
        return false;
    }

    @Override
    public Tracing currentTracing() {
        return NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }

    @Override
    public Metric getMetric() {
        return NoNull.of(metric, NoOpMetrics.NO_OP_METRIC);
    }

    @Override
    public <V> V getValue(Object key) {
        Object v = context.get(key);
        if (v == null) {
            return null;
        }
        return (V) v;
    }

    @Override
    public AsyncContext exportAsync(Request request) {
        return currentTracing().exportAsync(request);
    }

    @Override
    public void importAsync(AsyncContext snapshot) {
        currentTracing().importAsync(snapshot);
    }

    @Override
    public ProgressContext nextProgress(Request request) {
        return currentTracing().nextProgress(request);
    }

    @Override
    public void importProgress(Request request) {
        currentTracing().importProgress(request);
    }

    @Override
    public void setMetric(Metric metric) {
        this.metric = NoNull.of(metric, NoOpMetrics.NO_OP_METRIC);
    }

    @Override
    public void setCurrentTracing(Tracing tracing) {
        this.tracing = NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
    }
}
