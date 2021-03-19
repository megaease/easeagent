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

package com.megaease.easeagent.metrics.jdbc;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.*;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.metrics.converter.MetricValueFetcher;
import com.megaease.easeagent.metrics.model.LastMinutesCounterGauge;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractJdbcMetric extends AbstractMetric implements AgentInterceptor {

    public static final String ERR_CON_METRIC_KEY = "err-con";

    public AbstractJdbcMetric(MetricRegistry metricRegistry) {
        super(metricRegistry);
        this.metricNameFactory = MetricNameFactory.createBuilder()
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
    }

    @Override
    public void before(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        chain.doBefore(methodInfo, context);
    }

    protected void collectMetric(String key, boolean success, Map<Object, Object> context) {
        Timer timer = this.metricRegistry.timer(this.metricNameFactory.timerName(key, MetricSubType.DEFAULT));
        timer.update(Duration.ofMillis(ContextUtils.getDuration(context)));
        Counter counter = this.metricRegistry.counter(this.metricNameFactory.counterName(key, MetricSubType.DEFAULT));
        Meter meter = this.metricRegistry.meter(this.metricNameFactory.meterName(key, MetricSubType.DEFAULT));
        meter.mark();
        counter.inc();
        if (!success) {
            Counter errCounter = this.metricRegistry.counter(this.metricNameFactory.counterName(key, MetricSubType.ERROR));
            Meter errMeter = this.metricRegistry.meter(this.metricNameFactory.meterName(key, MetricSubType.ERROR));
            errMeter.mark();
            errCounter.inc();
        }
        MetricName gaugeName = metricNameFactory.gaugeNames(key).get(MetricSubType.DEFAULT);
        metricRegistry.gauge(gaugeName.name(), () -> () -> LastMinutesCounterGauge.builder()
                .m1Count((long) meter.getOneMinuteRate() * 60)
                .m5Count((long) meter.getFiveMinuteRate() * 60 * 5)
                .m15Count((long) meter.getFifteenMinuteRate() * 60 * 15)
                .build());

    }
    protected class JDBCConverter extends ConverterAdapter {
        public JDBCConverter(String category, String type, String keyFieldName, Map<String, Object> attributes) {
            super(category, type, metricNameFactory, KeyType.Timer, attributes, keyFieldName);
        }
    }
}
