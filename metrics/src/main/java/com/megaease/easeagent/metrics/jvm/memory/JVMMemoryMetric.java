/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.metrics.jvm.memory;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.metrics.*;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.metrics.model.JVMMemoryGaugeMetricModel;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class JVMMemoryMetric extends AbstractMetric implements ScheduleRunner {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final String POOLS = "pools";

    public JVMMemoryMetric(MetricRegistry metricRegistry) {
        this(metricRegistry, true);
    }

    public JVMMemoryMetric(MetricRegistry metricRegistry, boolean enableSchedule) {
        super(metricRegistry, enableSchedule);
        this.metricNameFactory = MetricNameFactory.createBuilder().gaugeType(MetricSubType.DEFAULT, new HashMap<>())
                .build();
    }

    @Override
    public Converter newConverter(Supplier<Map<String, Object>> attributes) {
        return new JVMMemoryMetricConverter(attributes);
    }

    @Override
    public void doJob() {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
            String memoryPoolMXBeanName = memoryPoolMXBean.getName();
            final String poolName = MetricRegistry.name(POOLS, WHITESPACE.matcher(memoryPoolMXBeanName).replaceAll("-"));
            Map<MetricSubType, MetricName> map = this.metricNameFactory.gaugeNames(poolName);
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

    class JVMMemoryMetricConverter extends ConverterAdapter {
        JVMMemoryMetricConverter(Supplier<Map<String, Object>> attributes) {
            super("application", "jvm-memory", metricNameFactory, KeyType.Gauge, attributes, "resource");
        }
    }
}
