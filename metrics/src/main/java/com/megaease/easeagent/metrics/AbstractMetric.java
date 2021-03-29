package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.concurrent.ScheduleHelper;
import com.megaease.easeagent.metrics.converter.Converter;

import java.util.Map;
import java.util.function.Supplier;

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
            ScheduleHelper.DEFAULT.execute(5, 10, obj::doJob);
        }
    }

    public abstract Converter newConverter(Supplier<Map<String, Object>> attributes);
}
