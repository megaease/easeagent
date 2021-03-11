package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.MetricRegistry;

public class SpringRedisMetricInterceptor extends BaseSpringRedisMetricInterceptor {

    public SpringRedisMetricInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

}
