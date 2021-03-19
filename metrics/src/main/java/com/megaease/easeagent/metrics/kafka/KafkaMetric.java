package com.megaease.easeagent.metrics.kafka;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.metrics.*;

import java.util.concurrent.TimeUnit;

public class KafkaMetric extends AbstractMetric {

    public KafkaMetric(MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.metricNameFactory = MetricNameFactory.createBuilder()
                .counterType(MetricSubType.PRODUCER, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_PRODUCER_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .counterType(MetricSubType.PRODUCER_ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_PRODUCER_ERROR_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .meterType(MetricSubType.PRODUCER, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.PRODUCER_M1_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.PRODUCER_M5_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.PRODUCER_M15_RATE, MetricValueFetcher.MeteredM15Rate)
                        .build())
                .meterType(MetricSubType.PRODUCER_ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.PRODUCER_M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.PRODUCER_M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.PRODUCER_M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                        .build())
                .timerType(MetricSubType.PRODUCER,
                        ImmutableMap.<MetricField, MetricValueFetcher>builder()
                                .put(MetricField.PRODUCER_MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
                                .put(MetricField.PRODUCER_MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
                                .put(MetricField.PRODUCER_MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
                                .put(MetricField.PRODUCER_P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
                                .put(MetricField.PRODUCER_P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
                                .put(MetricField.PRODUCER_P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
                                .put(MetricField.PRODUCER_P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
                                .put(MetricField.PRODUCER_P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
                                .put(MetricField.PRODUCER_P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
                                .put(MetricField.PRODUCER_P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
                                .build())
                .counterType(MetricSubType.CONSUMER, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_CONSUMER_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .counterType(MetricSubType.CONSUMER_ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_CONSUMER_ERROR_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .timerType(MetricSubType.CONSUMER,
                        ImmutableMap.<MetricField, MetricValueFetcher>builder()
                                .put(MetricField.CONSUMER_MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
                                .put(MetricField.CONSUMER_MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
                                .put(MetricField.CONSUMER_MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
                                .put(MetricField.CONSUMER_P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
                                .put(MetricField.CONSUMER_P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50PercentileValue)
                                .put(MetricField.CONSUMER_P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75PercentileValue)
                                .put(MetricField.CONSUMER_P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95PercentileValue)
                                .put(MetricField.CONSUMER_P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98PercentileValue)
                                .put(MetricField.CONSUMER_P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99PercentileValue)
                                .put(MetricField.CONSUMER_P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999PercentileValue)
                                .build())
                .meterType(MetricSubType.CONSUMER, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.CONSUMER_M1_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.CONSUMER_M5_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.CONSUMER_M15_RATE, MetricValueFetcher.MeteredM15Rate)
                        .build())
                .meterType(MetricSubType.CONSUMER_ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.CONSUMER_M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.CONSUMER_M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.CONSUMER_M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                        .build())

                .build();
    }

    public void meter(String topic, MetricSubType... meterTypes) {
        for (MetricSubType meterType : meterTypes) {
            Meter meter = this.metricRegistry.meter(metricNameFactory.meterName(topic, meterType));
            if (meter != null) {
                meter.mark();
            }
        }
    }

    void producerStop(long beginTime, String topic) {
        meter(topic, MetricSubType.PRODUCER);
        Timer timer = this.metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.PRODUCER));
        timer.update(beginTime, TimeUnit.MILLISECONDS);
        Counter counter = metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.PRODUCER));
        counter.inc();
    }

    public void errorProducer(String topic) {
        meter(topic, MetricSubType.PRODUCER_ERROR);
        Counter counter = metricRegistry.counter(this.metricNameFactory.counterName(topic, MetricSubType.PRODUCER_ERROR));
        counter.inc();
    }

    public Timer.Context consumeStart(String topic) {
        meter(topic, MetricSubType.CONSUMER);// meter
        Timer timer = this.metricRegistry.timer(metricNameFactory.timerName(topic, MetricSubType.CONSUMER)); //timer
        return timer.time();
    }

    public void consumeStop(Timer.Context context, String topic) {
        context.stop();
        Counter counter = metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER));
        counter.inc();
    }

    public void consumeError(String topic) {
        meter(topic, MetricSubType.CONSUMER_ERROR);
        Counter errorCounter = metricRegistry.counter(metricNameFactory.counterName(topic, MetricSubType.CONSUMER_ERROR));
        errorCounter.inc();
    }
}
