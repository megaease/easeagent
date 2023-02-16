package com.megaease.easeagent.plugin.sofarpc.interceptor.metrics;

import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.sofarpc.SofaRpcTags;

public abstract class SofaRpcMetricsBaseInterceptor implements NonReentrantInterceptor {
    public static volatile SofaRpcMetrics SOFARPC_METRICS;

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
        Tags tags = new Tags("application", ConfigConst.Namespace.SOFARPC, SofaRpcTags.METRICS_KEY.name);
        SOFARPC_METRICS = ServiceMetricRegistry.getOrCreate(config, tags, SofaRpcMetrics.SOFARPC_METRICS_SUPPLIER);
    }
}
