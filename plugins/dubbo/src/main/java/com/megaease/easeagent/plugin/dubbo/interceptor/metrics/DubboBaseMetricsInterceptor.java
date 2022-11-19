package com.megaease.easeagent.plugin.dubbo.interceptor.metrics;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

public abstract class DubboBaseMetricsInterceptor implements Interceptor {
    protected static volatile DubboMetrics DUBBO_METRICS;

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        Tags tags = new Tags("application", ConfigConst.Namespace.DUBBO, "service");
        DUBBO_METRICS = ServiceMetricRegistry.getOrCreate(config, tags, DubboMetrics.DUBBO_METRICS_SUPPLIER);
    }
}
