package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

public abstract class MotanBaseMetricsInterceptor implements Interceptor {
    public static volatile MotanMetric MOTAN_METRIC;

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        Tags tags = new Tags(MotanMetricTags.CATEGORY.name, MotanMetricTags.TYPE.name, MotanMetricTags.LABEL_NAME.name);
        MOTAN_METRIC = ServiceMetricRegistry.getOrCreate(config, tags, MotanMetric.MOTAN_METRIC_SUPPLIER);
    }

}
