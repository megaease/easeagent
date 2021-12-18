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

package com.megaease.easeagent.metrics.jvm.gc;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.metrics.*;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.function.Supplier;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;

public class JVMGCMetric extends AbstractMetric {
    public static final String ENABLE_KEY = "plugin.observability.jvmGc.metric.enabled";
    private static final String NO_GC = "No GC";
    private final Config config;

    public JVMGCMetric(MetricRegistry metricRegistry, Config config) {
        super(metricRegistry, true);
        this.config = config;
        this.nameFactory = NameFactory.createBuilder()
                .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.TIMES, MetricValueFetcher.MeteredCount)
                        .put(MetricField.TIMES_RATE, MetricValueFetcher.MeteredMeanRate)
                        .build())
                .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.TOTAL_COLLECTION_TIME, MetricValueFetcher.CountingCount)
                        .build())
                .build();
        this.collect();
    }

    @Override
    public Converter newConverter(Supplier<Map<String, Object>> attributes) {
        return new JVMGCMetricConverter(attributes);
    }

    public void collect() {
        for (GarbageCollectorMXBean mBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(mBean instanceof NotificationEmitter)) {
                continue;
            }
            NotificationListener listener = getListener();
            NotificationEmitter notificationEmitter = (NotificationEmitter) mBean;
            notificationEmitter.addNotificationListener(listener, null, null);
        }
    }

    private NotificationListener getListener() {
        return (notification, ref) -> {
            if (!notification.getType().equals(GARBAGE_COLLECTION_NOTIFICATION)) {
                return;
            }
            if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
                return;
            }

            CompositeData cd = (CompositeData) notification.getUserData();
            GarbageCollectionNotificationInfo notificationInfo = GarbageCollectionNotificationInfo.from(cd);
            String gcCause = notificationInfo.getGcCause();
            GcInfo gcInfo = notificationInfo.getGcInfo();
            long duration = gcInfo.getDuration();

            String gcName = notificationInfo.getGcName();
            Map<MetricSubType, MetricName> meterNames = nameFactory.meterNames(gcName);
            meterNames.forEach((type, name) -> {
                Meter meter = metricRegistry.meter(name.name());
                if (!NO_GC.equals(gcCause)) {
                    meter.mark();
                }
            });

            Map<MetricSubType, MetricName> counterNames = nameFactory.counterNames(gcName);
            counterNames.forEach((type, name) -> {
                Counter count = metricRegistry.counter(name.name());
                if (!NO_GC.equals(gcCause)) {
                    count.inc(duration);
                }
            });

        };
    }

    class JVMGCMetricConverter extends ConverterAdapter {
        JVMGCMetricConverter(Supplier<Map<String, Object>> attributes) {
            super("application", "jvm-gc", nameFactory, KeyType.Meter, attributes, "resource");
        }
    }
}
