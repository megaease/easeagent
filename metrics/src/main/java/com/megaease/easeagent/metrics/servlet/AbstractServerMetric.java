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

package com.megaease.easeagent.metrics.servlet;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.AbstractMetric;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.MetricValueFetcher;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.utils.metrics.ErrorPercentModelGauge;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractServerMetric extends AbstractMetric {

    public AbstractServerMetric(MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.nameFactory = NameFactory.createBuilder()
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

    public void collectMetric(String key, int statusCode, Throwable throwable, Map<Object, Object> context) {
        Timer timer = metricRegistry.timer(nameFactory.timerName(key, MetricSubType.DEFAULT));
        timer.update(Duration.ofMillis(ContextUtils.getDuration(context)));
        final Meter errorMeter = metricRegistry.meter(nameFactory.meterName(key, MetricSubType.ERROR));
        final Meter meter = metricRegistry.meter(nameFactory.meterName(key, MetricSubType.DEFAULT));
        Counter errorCounter = metricRegistry.counter(nameFactory.counterName(key, MetricSubType.ERROR));
        Counter counter = metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT));
        boolean hasException = throwable != null;
        if (statusCode >= 400 || hasException) {
            errorMeter.mark();
            errorCounter.inc();
        }
        counter.inc();
        meter.mark();

        metricRegistry.gauge(nameFactory.gaugeName(key, MetricSubType.DEFAULT), () -> () -> {
            BigDecimal m1ErrorPercent = BigDecimal.ZERO;
            BigDecimal m5ErrorPercent = BigDecimal.ZERO;
            BigDecimal m15ErrorPercent = BigDecimal.ZERO;
            BigDecimal error = BigDecimal.valueOf(errorMeter.getOneMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            BigDecimal n = BigDecimal.valueOf(meter.getOneMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m1ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }
            error = BigDecimal.valueOf(errorMeter.getFiveMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            n = BigDecimal.valueOf(meter.getFiveMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m5ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }

            error = BigDecimal.valueOf(errorMeter.getFifteenMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
            n = BigDecimal.valueOf(meter.getFifteenMinuteRate());
            if (n.compareTo(BigDecimal.ZERO) != 0) {
                m15ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
            }
            return new ErrorPercentModelGauge(m1ErrorPercent, m5ErrorPercent, m15ErrorPercent);
        });
    }

    @Override
    public Converter newConverter(Supplier<Map<String, Object>> attributes) {
        return new ServerConverter("application", "http-request", "url",
                attributes);
    }

    protected class ServerConverter extends ConverterAdapter {
        ServerConverter(String category, String type, String keyFieldName, Supplier<Map<String, Object>> attributes) {
            super(category, type, nameFactory, KeyType.Timer, attributes, keyFieldName);
        }
    }
}
