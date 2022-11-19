package com.megaease.easeagent.plugin.motan.interceptor.metrics;

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricSupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

public abstract class MotanBaseMetricsInterceptor implements Interceptor {
    protected MotanMetric motanMetric;

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
        Tags tags = new Tags("application", "motan", "service");
        motanMetric = ServiceMetricRegistry.getOrCreate(config, tags, new ServiceMetricSupplier<MotanMetric>() {
            @Override
            public NameFactory newNameFactory() {
                return MotanMetric.nameFactory();
            }

            @Override
            public MotanMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
                return new MotanMetric(metricRegistry, nameFactory);
            }
        });
    }

}
