package com.megaease.easeagent.metrics.jvm.gc;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.metrics.*;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.converter.MetricValueFetcher;
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

    private static final String NO_GC = "No GC";

    public JVMGCMetric(MetricRegistry metricRegistry) {
        super(metricRegistry,true);
        this.metricNameFactory = MetricNameFactory.createBuilder()
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
        //todo
        throw new UnsupportedOperationException();
    }

    public void collect() {
        for (GarbageCollectorMXBean mbean : ManagementFactory.getGarbageCollectorMXBeans()) {
            if (!(mbean instanceof NotificationEmitter)) {
                continue;
            }
            NotificationListener listener = getListener();
            NotificationEmitter notificationEmitter = (NotificationEmitter) mbean;
            notificationEmitter.addNotificationListener(listener, null, null);
        }
    }

    private NotificationListener getListener() {
        return (notification, ref) -> {
            if (!notification.getType().equals(GARBAGE_COLLECTION_NOTIFICATION)) {
                return;
            }

            CompositeData cd = (CompositeData) notification.getUserData();
            GarbageCollectionNotificationInfo notificationInfo = GarbageCollectionNotificationInfo.from(cd);
            String gcCause = notificationInfo.getGcCause();
            GcInfo gcInfo = notificationInfo.getGcInfo();
            long duration = gcInfo.getDuration();

            String gcName = notificationInfo.getGcName();
            Map<MetricSubType, MetricName> meterNames = metricNameFactory.meterNames(gcName);
            meterNames.forEach((type, name) -> {
                Meter meter = metricRegistry.meter(name.name());
                if (!NO_GC.equals(gcCause)) {
                    meter.mark();
                }
            });

            Map<MetricSubType, MetricName> counterNames = metricNameFactory.counterNames(gcName);
            counterNames.forEach((type, name) -> {
                Counter count = metricRegistry.counter(name.name());
                if (!NO_GC.equals(gcCause)) {
                    count.inc(duration);
                }
            });

        };
    }

}
