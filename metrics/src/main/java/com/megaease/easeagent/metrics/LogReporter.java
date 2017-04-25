package com.megaease.easeagent.metrics;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class LogReporter implements Runnable {
    private static final String START_TIME = ManagementFactory.getRuntimeMXBean().getStartTime() + "";

    private final Logger logger;
    private final MetricRegistry registry;
    private final Map<String, String> hostInfo;
    private final TimeUnit rate;
    private final TimeUnit duration;

    LogReporter(Logger logger, MetricRegistry registry, Map<String, String> hostInfo, TimeUnit rate, TimeUnit duration) {
        this.logger = logger;
        this.registry = registry;
        this.hostInfo = hostInfo;
        this.rate = rate;
        this.duration = duration;
    }

    @Override
    public void run() {
        for (Map.Entry<String, Metric> entry : registry.getMetrics().entrySet()) {
            final Iterator<String> iterator = Splitter.on(':').limit(2).split(entry.getKey()).iterator();
            final String name = iterator.next();
            final Map<String, String> tags = ImmutableMap.<String, String>builder()
                    .putAll(Splitter.on(',').withKeyValueSeparator('=').split(iterator.next()))
                    .putAll(hostInfo)
                    .build();
            final MetricEvent event = new MetricEvent(entry.getValue(), name, tags, rate, duration);
            logger.info("{}\n", JSON.toJSONString(event, SerializerFeature.DisableCircularReferenceDetect));
        }
    }
}
