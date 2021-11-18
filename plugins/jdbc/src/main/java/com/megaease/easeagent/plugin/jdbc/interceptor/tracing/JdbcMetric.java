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

package com.megaease.easeagent.plugin.jdbc.interceptor.tracing;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.AbstractMetric;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.utils.ImmutableMap;

import java.util.HashMap;

public class JdbcMetric extends AbstractMetric {
    public JdbcMetric(Config config, Tags tags) {
        super(config, tags);
        this.nameFactory = getNameFactory();
        this.metricRegistry = EaseAgent.newMetricRegistry(config, this.nameFactory, tags);
    }

    public NameFactory getNameFactory() {
        NameFactory nameFactory = NameFactory.createBuilder()
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
            .gaugeType(MetricSubType.DEFAULT, new HashMap<>())
            .meterType(MetricSubType.DEFAULT,
                ImmutableMap.<MetricField, MetricValueFetcher>builder()
                    .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
                    .put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
                    .put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
                    .build())
            .meterType(MetricSubType.ERROR,
                ImmutableMap.<MetricField, MetricValueFetcher>builder()
                    .put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                    .put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                    .put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                    .build())
            .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
                .build())
            .counterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
                .build())
            .build();
        return nameFactory;
    }
}
