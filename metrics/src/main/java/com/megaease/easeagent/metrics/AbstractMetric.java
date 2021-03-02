package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;

import java.util.Map;

public abstract class AbstractMetric implements AgentInterceptor {

    protected MetricRegistry metricRegistry;

    protected MetricNameFactory metricNameFactory;

    public static final String BEGIN_TIME = "beginTime";

    protected Long getBeginTime(Map<Object, Object> context) {
        return (Long) context.get(BEGIN_TIME);
    }

    protected void setBeginTime(Map<Object, Object> context) {
        context.put(BEGIN_TIME, System.currentTimeMillis());
    }

    @Override
    public void before(Object invoker, String method, Object[] args, Map<Object, Object> context) {
        this.setBeginTime(context);
    }
}
