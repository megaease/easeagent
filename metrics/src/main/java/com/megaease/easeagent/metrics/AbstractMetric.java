package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.AdditionalAttributes;
import com.megaease.easeagent.core.AgentThreadFactory;
import com.megaease.easeagent.metrics.converter.Converter;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMetric {

    protected MetricRegistry metricRegistry;

    protected MetricNameFactory metricNameFactory;

    protected boolean enableSchedule;

    public AbstractMetric(MetricRegistry metricRegistry) {
        this(metricRegistry, false);
    }

    public AbstractMetric(MetricRegistry metricRegistry, boolean enableSchedule) {
        this.metricRegistry = metricRegistry;
        this.enableSchedule = enableSchedule;
        if (this.enableSchedule && this instanceof ScheduleRunner) {
            ScheduleRunner obj = (ScheduleRunner) this;
            ThreadFactory threadFactory = new AgentThreadFactory();
            Executors.newSingleThreadScheduledExecutor(threadFactory).scheduleWithFixedDelay(obj::doJob, 5, 10, TimeUnit.SECONDS);
        }
    }
    public abstract Converter newConverter(AdditionalAttributes attributes);
}
