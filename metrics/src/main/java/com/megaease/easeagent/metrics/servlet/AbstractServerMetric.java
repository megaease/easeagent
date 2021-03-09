package com.megaease.easeagent.metrics.servlet;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.metrics.*;

import java.util.HashMap;

public abstract class AbstractServerMetric extends AbstractMetric {

    public AbstractServerMetric(MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.metricNameFactory = MetricNameFactory.createBuilder()
                .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .meterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
                        .put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
                        .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                        .build())
                .counterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .meterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                        .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                        .build())
                .gaugeType(MetricSubType.DEFAULT, new HashMap<>())
                .timerType(MetricSubType.DEFAULT,
                        ImmutableMap.<MetricField, MetricValueFetcher>builder()
                                .put(MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
                                .put(MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
                                .put(MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
                                .put(MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
                                .put(MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
                                .put(MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
                                .put(MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
                                .put(MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
                                .put(MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
                                .put(MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
                                .build())
                .build();
    }

}
