/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.rabbitmq;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.utils.ImmutableMap;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RabbitMqProducerMetric {
    final NameFactory nameFactory;
    final MetricRegistry metric;

    public RabbitMqProducerMetric(Config config) {
        this.nameFactory = getNameFactory();
        Tags tags = new Tags("application", "rabbitmq-ex-ro", "resource");
        this.metric = EaseAgent.newMetricRegistry(config, this.nameFactory, tags);
    }

    public void metricAfter(String exchange, String routingKey, long beginTime, boolean success) {
        String key = String.join("-", exchange, routingKey);
        Map<MetricSubType, MetricName> timerNames = nameFactory.timerNames(key);
        MetricName name = timerNames.get(MetricSubType.DEFAULT);
        Timer timer = metric.timer(name.name());
        timer.update(System.currentTimeMillis() - beginTime, TimeUnit.MILLISECONDS);
        final Meter defaultMeter = metric.meter(nameFactory.meterName(key, MetricSubType.PRODUCER));
        final Meter errorMeter = metric.meter(nameFactory.meterName(key, MetricSubType.PRODUCER_ERROR));
        if (!success) {
            errorMeter.mark();
        }
        defaultMeter.mark();
    }

    public static NameFactory getNameFactory() {
        return NameFactory.createBuilder()
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
            .build();
    }
}
