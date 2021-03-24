package com.megaease.easeagent.metrics.redis;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
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

public class CommonRedisMetricInterceptor extends AbstractMetric implements AgentInterceptor {

    public CommonRedisMetricInterceptor(MetricRegistry metricRegistry) {
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

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        String key = methodInfo.getInvoker().getClass().getSimpleName() + "." + methodInfo.getMethod();
        metricRegistry.timer(this.metricNameFactory.timerName(key, MetricSubType.DEFAULT)).update(ContextUtils.getDuration(context), TimeUnit.MILLISECONDS);
        final Meter defaultMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.DEFAULT));
        final Counter defaultCounter = metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.DEFAULT));
        final Meter errorMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.ERROR));
        final Counter errorCounter = metricRegistry.counter(metricNameFactory.counterName(key, MetricSubType.ERROR));

        if (methodInfo.getThrowable() != null) {
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
        return chain.doAfter(methodInfo, context);
    }
}
