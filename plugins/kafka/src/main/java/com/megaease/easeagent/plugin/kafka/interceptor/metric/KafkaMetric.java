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

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.Counter;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.api.metric.name.*;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class KafkaMetric extends AbstractMetric {


    protected KafkaMetric(@Nonnull Config config, @Nonnull Tags tags) {
        super(config, tags);
    }

    public void meter(String topic, MetricSubType... meterTypes) {
        for (MetricSubType meterType : meterTypes) {
            Meter meter = this.metricRegistry.meter(nameFactory.meterName(topic, meterType));
            if (meter != null) {
                meter.mark();
            }
        }
    }

    void producerStop(long beginTime, String topic) {
        meter(topic, MetricSubType.PRODUCER);
        Timer timer = this.metricRegistry.timer(nameFactory.timerName(topic, MetricSubType.PRODUCER));
        timer.update(beginTime, TimeUnit.MILLISECONDS);
        Counter counter = metricRegistry.counter(nameFactory.counterName(topic, MetricSubType.PRODUCER));
        counter.inc();
    }

    public void errorProducer(String topic) {
        meter(topic, MetricSubType.PRODUCER_ERROR);
        Counter counter = metricRegistry.counter(this.nameFactory.counterName(topic, MetricSubType.PRODUCER_ERROR));
        counter.inc();
    }

    public Timer.Context consumeStart(String topic) {
        meter(topic, MetricSubType.CONSUMER);// meter
        Timer timer = this.metricRegistry.timer(nameFactory.timerName(topic, MetricSubType.CONSUMER)); //timer
        return timer.time();
    }

    public void consumeStop(Timer.Context context, String topic) {
        context.stop();
        Counter counter = metricRegistry.counter(nameFactory.counterName(topic, MetricSubType.CONSUMER));
        counter.inc();
    }

    public void consumeError(String topic) {
        meter(topic, MetricSubType.CONSUMER_ERROR);
        Counter errorCounter = metricRegistry.counter(nameFactory.counterName(topic, MetricSubType.CONSUMER_ERROR));
        errorCounter.inc();
    }

    public void consume(String topic, long beginTime, boolean success) {
        meter(topic, MetricSubType.CONSUMER);
        this.metricRegistry.timer(nameFactory.timerName(topic, MetricSubType.CONSUMER)).update(System.currentTimeMillis() - beginTime, TimeUnit.MILLISECONDS);
        Counter counter = metricRegistry.counter(nameFactory.counterName(topic, MetricSubType.CONSUMER));
        counter.inc();
        if (!success) {
            meter(topic, MetricSubType.CONSUMER_ERROR);
            Counter errorCounter = metricRegistry.counter(nameFactory.counterName(topic, MetricSubType.CONSUMER_ERROR));
            errorCounter.inc();
        }
    }


    @Nonnull
    @Override
    protected NameFactory nameFactory() {
        return NameFactory.createBuilder()
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
}
