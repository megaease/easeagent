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

public class RabbitMqProducerMetric extends AbstractMetric {

    public RabbitMqProducerMetric(MetricRegistry metricRegistry) {
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

    @Override
    public Converter newConverter(AdditionalAttributes attributes) {
        return new RabbitMqConverter("application", "rabbit", "queue",
                attributes.getAdditionalAttributes());
    }

    protected class RabbitMqConverter extends ConverterAdapter {

        public RabbitMqConverter(String category, String type, String keyFieldName, Map<String, Object> attributes) {
            super(category, type, metricNameFactory, KeyType.Timer, attributes, keyFieldName);
        }
    }

    public void after(String exchange, String routingKey, long beginTime, boolean success) {
        String key = String.join("-", exchange, routingKey);
        Map<MetricSubType, MetricName> timerNames = metricNameFactory.timerNames(key);
        MetricName name = timerNames.get(MetricSubType.DEFAULT);
        Timer timer = metricRegistry.timer(name.name());
        timer.update(System.currentTimeMillis() - beginTime, TimeUnit.MILLISECONDS);
        final Meter defaultMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER));
        final Meter errorMeter = metricRegistry.meter(metricNameFactory.meterName(key, MetricSubType.PRODUCER_ERROR));
        if (!success) {
            errorMeter.mark();
        }
        defaultMeter.mark();
    }
}
