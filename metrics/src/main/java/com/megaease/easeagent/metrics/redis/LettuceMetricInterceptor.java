package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.MethodInfo;

import java.util.Map;

public class LettuceMetricInterceptor extends AbstractRedisMetricInterceptor {

    public LettuceMetricInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public String getKey(MethodInfo methodInfo, Map<Object, Object> context) {
        return (String) context.get(ContextCons.CACHE_CMD);
    }

}
