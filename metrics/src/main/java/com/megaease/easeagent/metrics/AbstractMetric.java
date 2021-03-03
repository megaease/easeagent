package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;

public abstract class AbstractMetric implements AgentInterceptor {

    protected MetricRegistry metricRegistry;

    protected MetricNameFactory metricNameFactory;

}
