/*
 * Copyright (c) 2022, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.megaease.easeagent.metrics.jvm.memory;

import com.megaease.easeagent.metrics.model.JVMMemoryGaugeMetricModel;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.async.ScheduleHelper;
import com.megaease.easeagent.plugin.async.ScheduleRunner;

import javax.annotation.Nonnull;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class JVMMemoryMetricV2 extends ServiceMetric implements ScheduleRunner {
    private static final ServiceMetricSupplier<JVMMemoryMetricV2> SUPPLIER = new ServiceMetricSupplier<JVMMemoryMetricV2>() {
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
    private static IPluginConfig config;

    private JVMMemoryMetricV2(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
        super(metricRegistry, nameFactory);
    }

    public static JVMMemoryMetricV2 getMetric() {
        config = AutoRefreshPluginConfigRegistry.getOrCreate("observability", "jvmMemory", "metric");
        Tags tags = new Tags("application", "jvm-memory", "resource");

        JVMMemoryMetricV2 v2 = ServiceMetricRegistry.getOrCreate(config, tags, SUPPLIER);
        ScheduleHelper.DEFAULT.nonStopExecute(10, 10, v2::doJob);

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
