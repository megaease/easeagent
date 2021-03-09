package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class AbstractMetric {

    protected MetricRegistry metricRegistry;

    protected MetricNameFactory metricNameFactory;

    protected boolean enableSchedule;

    public AbstractMetric(MetricRegistry metricRegistry) {
        this(metricRegistry, true);
    }

    public AbstractMetric(MetricRegistry metricRegistry, boolean enableSchedule) {
        this.metricRegistry = metricRegistry;
        this.enableSchedule = enableSchedule;
        if (this.enableSchedule && this instanceof ScheduleRunner) {
            ScheduleRunner obj = (ScheduleRunner) this;
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(obj::doJob, 5, 10, TimeUnit.SECONDS);
        }
    }
}
