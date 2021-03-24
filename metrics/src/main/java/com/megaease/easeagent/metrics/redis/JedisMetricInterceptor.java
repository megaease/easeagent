package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

public class JedisMetricInterceptor extends AbstractRedisMetricInterceptor {

    public JedisMetricInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public String getKey(MethodInfo methodInfo, Map<Object, Object> context) {
        return methodInfo.getMethod();
    }

}
