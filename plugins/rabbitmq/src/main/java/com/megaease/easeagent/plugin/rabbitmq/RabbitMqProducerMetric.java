/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.rabbitmq;

import com.megaease.easeagent.plugin.api.metric.*;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.utils.ImmutableMap;

import javax.annotation.Nonnull;
import java.util.concurrent.TimeUnit;

public class RabbitMqProducerMetric extends ServiceMetric {
    public static final ServiceMetricSupplier<RabbitMqProducerMetric> SERVICE_METRIC_SUPPLIER = new ServiceMetricSupplier<RabbitMqProducerMetric>() {
        @Override
        public NameFactory newNameFactory() {
            return buildNameFactory();
        }

        @Override
        public RabbitMqProducerMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
            return new RabbitMqProducerMetric(metricRegistry, nameFactory);
        }
    };

    public RabbitMqProducerMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
        super(metricRegistry, nameFactory);
    }

    public void metricAfter(String exchange, String routingKey, long beginTime, boolean success) {
        String key = String.join("-", exchange, routingKey);
        Timer timer = timer(key, MetricSubType.DEFAULT);
        timer.update(System.currentTimeMillis() - beginTime, TimeUnit.MILLISECONDS);
        final Meter defaultMeter = meter(key, MetricSubType.PRODUCER);
        final Meter errorMeter = meter(key, MetricSubType.PRODUCER_ERROR);
        if (!success) {
            errorMeter.mark();
        }
        defaultMeter.mark();
    }

    public static Tags buildTags() {
        Tags tags = new Tags("application", "rabbitmq-ex-ro", "resource");
        RedirectProcessor.setTagsIfRedirected(Redirect.RABBITMQ, tags);
        return tags;
    }

    public static NameFactory buildNameFactory() {
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
