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

package com.megaease.easeagent.metrics.rabbitmq;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.AdditionalAttributes;
import com.megaease.easeagent.metrics.*;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.metrics.converter.MetricValueFetcher;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class RabbitMqConsumerMetric extends AbstractMetric {

    public RabbitMqConsumerMetric(MetricRegistry metricRegistry) {
        super(metricRegistry);
        metricNameFactory = MetricNameFactory.createBuilder()
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
                .meterType(MetricSubType.CONSUMER, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.QUEUE_M1_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.QUEUE_M5_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.QUEUE_M15_RATE, MetricValueFetcher.MeteredM15Rate)
                        .build())
                .meterType(MetricSubType.CONSUMER_ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.QUEUE_M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                        .put(MetricField.QUEUE_M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                        .put(MetricField.QUEUE_M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                        .build())
                .build();
    }

    @Override
    public Converter newConverter(Supplier<Map<String, Object>> attributes) {
        return new RabbitMqConverter(attributes);
    }

    protected class RabbitMqConverter extends ConverterAdapter {

        public RabbitMqConverter(Supplier<Map<String, Object>> attributes) {
            super("application", "rabbitmq-queue", metricNameFactory, KeyType.Timer, attributes, "resource");
        }
    }

    public void after(String queue, long beginTime, boolean success) {
        Map<MetricSubType, MetricName> timerNames = metricNameFactory.timerNames(queue);
        MetricName metricName = timerNames.get(MetricSubType.DEFAULT);
        Timer timer = metricRegistry.timer(metricName.name());
        timer.update(System.currentTimeMillis() - beginTime, TimeUnit.MILLISECONDS);
        final Meter defaultMeter = metricRegistry.meter(metricNameFactory.meterName(queue, MetricSubType.CONSUMER));
        final Meter errorMeter = metricRegistry.meter(metricNameFactory.meterName(queue, MetricSubType.CONSUMER_ERROR));
        if (!success) {
            errorMeter.mark();
        }
        defaultMeter.mark();
    }
}
