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

package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import com.megaease.easeagent.metrics.*;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.metrics.converter.ConverterAdapter;
import com.megaease.easeagent.metrics.converter.KeyType;
import com.megaease.easeagent.metrics.converter.MetricValueFetcher;
import com.megaease.easeagent.metrics.model.LastMinutesCounterGauge;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractRedisMetricInterceptor extends AbstractMetric implements AgentInterceptor {
    public static final String ENABLE_KEY = "observability.metrics.redis.enabled";
    private final Config config;

    public AbstractRedisMetricInterceptor(MetricRegistry metricRegistry, Config config) {
        super(metricRegistry);
        this.config = config;
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
                                .put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1Rate)
                                .put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
                                .put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
                                .put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
                                .build())
                .meterType(MetricSubType.ERROR,
                        ImmutableMap.<MetricField, MetricValueFetcher>builder()
                                .put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
                                .put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
                                .put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
                                .build())
                .counterType(MetricSubType.ERROR, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .counterType(MetricSubType.DEFAULT, ImmutableMap.<MetricField, MetricValueFetcher>builder()
                        .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
                        .build())
                .build();
    }

    @Override
    public Converter newConverter(Supplier<Map<String, Object>> attributes) {
        return new RedisConverter(attributes);
    }

    protected class RedisConverter extends ConverterAdapter {
        public RedisConverter(Supplier<Map<String, Object>> attributes) {
            super("application", "cache-redis", metricNameFactory, KeyType.Timer, attributes, "signature");
        }
    }

    public abstract String getKey(MethodInfo methodInfo, Map<Object, Object> context);

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String key = this.getKey(methodInfo, context);
        this.collect(key, ContextUtils.getDuration(context), methodInfo.isSuccess());
        return chain.doAfter(methodInfo, context);
    }

    public void collect(String key, long duration, boolean success) {
        if (!SwitchUtil.enableMetric(config, ENABLE_KEY)) {
            return;
        }
        metricRegistry.timer(this.metricNameFactory.timerName(key, MetricSubType.DEFAULT)).update(duration, TimeUnit.MILLISECONDS);
        final Meter defaultMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT));
        final Counter defaultCounter = metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT));
        final Meter errorMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR));
        final Counter errorCounter = metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR));

        if (!success) {
            errorMeter.mark();
            errorCounter.inc();
        }
        defaultMeter.mark();
        defaultCounter.inc();

        MetricName gaugeName = metricNameFactory.gaugeNames(key).get(MetricSubType.DEFAULT);
        metricRegistry.gauge(gaugeName.name(), () -> () ->
                LastMinutesCounterGauge.builder()
                        .m1Count((long) (defaultMeter.getOneMinuteRate() * 60))
                        .m5Count((long) (defaultMeter.getFiveMinuteRate() * 60 * 5))
                        .m15Count((long) (defaultMeter.getFifteenMinuteRate() * 60 * 15))
                        .build());
    }
}
