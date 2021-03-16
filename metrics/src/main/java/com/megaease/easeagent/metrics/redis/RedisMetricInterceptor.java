package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.MetricRegistry;

public class RedisMetricInterceptor extends CommonRedisMetricInterceptor {

    public RedisMetricInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

}
