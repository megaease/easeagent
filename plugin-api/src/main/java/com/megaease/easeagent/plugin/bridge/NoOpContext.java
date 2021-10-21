package com.megaease.easeagent.plugin.bridge;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.context.AsyncContext;
import com.megaease.easeagent.plugin.api.context.ProgressContext;
import com.megaease.easeagent.plugin.api.metric.Metric;
import com.megaease.easeagent.plugin.api.metric.MetricContext;
import com.megaease.easeagent.plugin.api.trace.Request;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.api.trace.TraceContext;
import com.megaease.easeagent.plugin.api.trace.Tracing;

import java.util.Collections;
import java.util.Map;

public class NoOpContext {
    public static final Context NO_OP_CONTEXT = NoopContext.INSTANCE;
    public static final EmptyAsyncContext NO_OP_ASYNC_CONTEXT = EmptyAsyncContext.INSTANCE;
    public static final NoopProgressContext NO_OP_PROGRESS_CONTEXT = NoopProgressContext.INSTANCE;

    private static class NoopContext implements Context, MetricContext, TraceContext {
        private static final NoopContext INSTANCE = new NoopContext();

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public Tracing currentTracing() {
            return NoOpTracer.NO_OP_TRACING;
        }

        @Override
        public Metric getMetric() {
            return NoOpMetrics.NO_OP_METRIC;
        }

        @Override
        public <V> V getValue(Object key) {
            return null;
        }

        @Override
        public AsyncContext exportAsync(Request request) {
            return EmptyAsyncContext.INSTANCE;
        }

        @Override
        public void importAsync(AsyncContext snapshot) {

        }

        @Override
        public ProgressContext nextProgress(Request request) {
            return NoopProgressContext.INSTANCE;
        }

        @Override
        public void importProgress(Request request) {

        }


        @Override
        public void setMetric(Metric metric) {

        }

        @Override
        public void setCurrentTracing(Tracing tracing) {

        }
    }


    private static class EmptyAsyncContext implements AsyncContext {
        private static final EmptyAsyncContext INSTANCE = new EmptyAsyncContext();

        @Override
        public boolean isNoop() {
            return true;
        }

        @Override
        public Tracing getTracer() {
            return NoOpTracer.NO_OP_TRACING;
        }

        @Override
        public Map<String, Object> getContext() {
            return Collections.emptyMap();
        }
    }

    private static class NoopProgressContext implements ProgressContext {
        private static final NoopProgressContext INSTANCE = new NoopProgressContext();

        @Override
        public Span span() {
            return NoOpTracer.NO_OP_SPAN;
        }

        @Override
        public Map<String, String> getHeader() {
            return Collections.emptyMap();
        }
    }

}
