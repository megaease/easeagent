package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricContext;
import com.megaease.easeagent.plugin.api.trace.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Tracer;

public class NoOpContext {
    public static final Context NO_OP_CONTEXT = NoopContext.INSTANCE;

    private static class NoopContext implements Context, MetricContext, TraceContext {
        private static final NoopContext INSTANCE = new NoopContext();

        @Override
        public Tracer currentTrace() {
            return NoOpTracer.NO_OP_TRACER;
        }

        @Override
        public Metric getMetric() {
            return NoOpMetrics.NO_OP_METRIC;
        }

        @Override
        public void setMetric(Metric metric) {
            
        }

        @Override
        public void setCurrentTrace(Tracer tracer) {

        }
    }

}
