package com.megaease.easeagent.metrics.jvm.memory;

import com.megaease.easeagent.metrics.model.JVMMemoryGaugeMetricModel;
import com.megaease.easeagent.plugin.api.config.AutoRefreshRegistry;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.MetricName;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.async.ScheduleRunner;

import javax.annotation.Nonnull;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class JVMMemoryMetricV2 extends ServiceMetric implements ScheduleRunner {
    public static final ServiceMetricSupplier<JVMMemoryMetricV2> SUPPLIER = new ServiceMetricSupplier<JVMMemoryMetricV2>() {
        @Override
        public NameFactory newNameFactory() {
            return JVMMemoryMetricV2.nameFactory();
        }

        @Override
        public JVMMemoryMetricV2 newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
            return new JVMMemoryMetricV2(metricRegistry, nameFactory);
        }
    };

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final String POOLS = "pools";
    private static Config config;

    public JVMMemoryMetricV2(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
        super(metricRegistry, nameFactory);
    }

    public static JVMMemoryMetricV2 getMetric() {
        config = AutoRefreshRegistry.getOrCreate("observability", "jvmMemory", "metric");
        Tags tags = new Tags("application", "jvm-memory", "resource");

        JVMMemoryMetricV2 v2 = ServiceMetricRegistry.getOrCreate(config, tags, SUPPLIER);

        return v2;
    }

    static NameFactory nameFactory() {
        return NameFactory.createBuilder()
            .gaugeType(MetricSubType.DEFAULT, new HashMap<>())
            .build();
    }

    @Override
    public void doJob() {
        if (!config.enabled()) {
            return;
        }
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String memoryPoolMXBeanName = memoryPoolMXBean.getName();

            final String poolName = com.codahale.metrics.MetricRegistry
                .name(POOLS, WHITESPACE.matcher(memoryPoolMXBeanName).replaceAll("-"));

            Map<MetricSubType, MetricName> map = this.nameFactory.gaugeNames(poolName);
            for (Map.Entry<MetricSubType, MetricName> entry : map.entrySet()) {
                MetricName metricName = entry.getValue();

                Gauge<JVMMemoryGaugeMetricModel> gauge = () -> new JVMMemoryGaugeMetricModel(
                    memoryPoolMXBean.getUsage().getInit(),
                    memoryPoolMXBean.getUsage().getUsed(),
                    memoryPoolMXBean.getUsage().getCommitted(),
                    memoryPoolMXBean.getUsage().getMax());

                this.metricRegistry.gauge(metricName.name(), () -> gauge);
            }
        }
    }
}
