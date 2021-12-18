package com.megaease.easeagent.metrics.jvm.gc;

import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.plugin.api.config.AutoRefreshRegistry;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.GcInfo;

import javax.annotation.Nonnull;
import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.Map;

import static com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION;

public class JVMGCMetricV2 extends ServiceMetric {
    private static final String NO_GC = "No GC";

    public static final ServiceMetricSupplier<JVMGCMetricV2> METRIC_SUPPLIER = new ServiceMetricSupplier<JVMGCMetricV2>() {
        @Override
        public NameFactory newNameFactory() {
            return JVMGCMetricV2.nameFactory();
        }

        @Override
        public JVMGCMetricV2 newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
            return new JVMGCMetricV2(metricRegistry, nameFactory);
        }
    };

    private static Config config;

    public static JVMGCMetricV2 getMetric() {
        config = AutoRefreshRegistry.getOrCreate("observability", "jvmGc", "metric");
        Tags tags = new Tags("application", "jvm-gc", "resource");

        JVMGCMetricV2 v2 = ServiceMetricRegistry.getOrCreate(config, tags, METRIC_SUPPLIER);
        v2.collect();

        return v2;
    }

    public JVMGCMetricV2(@Nonnull MetricRegistry metricRegistry,
                         @Nonnull NameFactory nameFactory) {
        super(metricRegistry, nameFactory);
    }

    static NameFactory nameFactory() {
        return NameFactory.createBuilder()
            .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.TIMES, MetricValueFetcher.MeteredCount)
                .put(MetricField.TIMES_RATE, MetricValueFetcher.MeteredMeanRate)
                .build())
            .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.TOTAL_COLLECTION_TIME, MetricValueFetcher.CountingCount)
                .build())
            .build();
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

            if (!config.enabled()) {
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
}
