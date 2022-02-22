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

package com.megaease.easeagent.metrics.converter;

import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.megaease.easeagent.metrics.impl.CounterImpl;
import com.megaease.easeagent.metrics.impl.MeterImpl;
import com.megaease.easeagent.metrics.impl.SnapshotImpl;
import com.megaease.easeagent.metrics.impl.TimerImpl;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.tools.metrics.GaugeMetricModel;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ConverterAdapter extends AbstractConverter {

    private final List<KeyType> keyTypes;

    private final NameFactory nameFactory;

    public ConverterAdapter(String category, String type, NameFactory metricNameFactory, KeyType keyType,
                            Supplier<Map<String, Object>> attributes, String keyFieldName) {
        super(category, type, keyFieldName, attributes);
        this.keyTypes = Collections.singletonList(keyType);
        this.nameFactory = metricNameFactory;
    }

    public ConverterAdapter(NameFactory metricNameFactory, List<KeyType> keyTypes,
                            Supplier<Map<String, Object>> attributes, Tags tags) {
        super(attributes, tags);
        this.keyTypes = Collections.unmodifiableList(keyTypes);
        this.nameFactory = metricNameFactory;
    }

    public ConverterAdapter(String category, String type, NameFactory metricNameFactory, KeyType keyType,
                            Supplier<Map<String, Object>> attributes) {
        this(category, type, metricNameFactory, keyType, attributes, "resource");
    }


    @Override
    @SuppressWarnings("rawtypes")
    protected List<String> keysFromMetrics(SortedMap<String, Gauge> gauges,
                                           SortedMap<String, Counter> counters,
                                           SortedMap<String, Histogram> histograms,
                                           SortedMap<String, Meter> meters,
                                           SortedMap<String, Timer> timers) {
        Set<String> results = new HashSet<>();
        for (KeyType keyType : this.keyTypes) {
            if (keyType != null) {
                switch (keyType) {
                    case Timer:
                        keys(timers.keySet(), results);
                        break;
                    case Histogram:
                        keys(histograms.keySet(), results);
                        break;
                    case Gauge:
                        keys(gauges.keySet(), results);
                        break;
                    case Counter:
                        keys(counters.keySet(), results);
                        break;
                    case Meter:
                        keys(meters.keySet(), results);
                        break;
                    default:
                        //ignore
                }
            }
        }

        return new ArrayList<>(results);
    }

    private void keys(Set<String> origins, Set<String> results) {
        origins.forEach(s -> results.add(MetricName.metricNameFor(s).getKey()));
    }

    private double convertDuration(Long duration) {
        return (double) duration / durationFactor;
    }

    private double convertDuration(Double duration) {
        return duration / durationFactor;
    }

    private double convertRate(double rate) {
        return rate * rateFactor;
    }

    private double convertRate(Long rate) {
        return rate == null ? 0 : rate * rateFactor;
    }

    private void appendRate(Map<String, Object> output, String key, Object value, int scale) {
        if (value instanceof Long) {
            output.put(key, convertRate((Long) value));
        } else if (value instanceof Double) {
            output.put(key, toDouble(convertRate((Double) value), scale));
        }
    }

    private void appendDuration(Map<String, Object> output, String key, Object value, int scale) {
        if (value instanceof Long) {
            output.put(key, convertDuration((Long) value));
        } else if (value instanceof Double) {
            output.put(key, toDouble(convertDuration((Double) value), scale));
        }
    }

    private double toDouble(Double i, int scale) {
        return BigDecimal.valueOf(i).setScale(scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
    }


    @Override
    @SuppressWarnings("rawtypes")
    protected void writeGauges(String key, MetricSubType metricSubType, SortedMap<String, Gauge> gauges, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = nameFactory.gaugeNames(key);
        consumerMetric(map, metricSubType, v -> {
            Gauge gauge = gauges.get(v.name());
            if (gauge == null) {
                return;
            }
            Object value = gauge.getValue();
            if (value instanceof GaugeMetricModel) {
                GaugeMetricModel model = (GaugeMetricModel) value;
                output.putAll(model.toHashMap());
            } else if (value instanceof Number || value instanceof Boolean) {
                output.put("value", value);
            } else {
                output.put("value", value.toString());
            }
        });
    }

    protected static <T> void consumerMetric(Map<MetricSubType, T> map, MetricSubType metricSubType, Consumer<T> consumer) {
        if (metricSubType == null) {
            map.values().forEach(consumer);
        }
        T t = map.get(metricSubType);
        if (t != null) {
            consumer.accept(t);
        }
    }

    @Override
    protected void writeCounters(String key, MetricSubType metricSubType, SortedMap<String, Counter> counters, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = nameFactory.counterNames(key);
        consumerMetric(map, metricSubType, v -> Optional
            .ofNullable(counters.get(v.name()))
            .ifPresent(c -> v.getValueFetcher().forEach((fieldName, fetcher) -> appendField(output, fieldName, fetcher, CounterImpl.build(c)))));

    }

    @Override
    @SuppressWarnings("all")
    protected void writeHistograms(String key, MetricSubType metricSubType, SortedMap<String, Histogram> histograms, Map<String, Object> output) {
        //write histograms, Temporarily unsupported
        //Please use timer to calculate the time of P95, P99, etc
    }

    @Override
    protected void writeMeters(String key, MetricSubType metricSubType, SortedMap<String, Meter> meters, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = nameFactory.meterNames(key);
        consumerMetric(map, metricSubType, v -> Optional
            .ofNullable(meters.get(v.name()))
            .ifPresent(m -> v.getValueFetcher().forEach(
                (fieldName, fetcher) -> appendField(output, fieldName, fetcher, MeterImpl.build(m))))
        );
    }

    private void appendField(Map<String, Object> output, MetricField fieldName, MetricValueFetcher fetcher,
                             com.megaease.easeagent.plugin.api.metric.Metric metric) {
        switch (fieldName.getType()) {
            case DURATION:
                appendDuration(output, fieldName.getField(), fetcher.apply(metric), fieldName.getScale());
                break;
            case RATE:
                appendRate(output, fieldName.getField(), fetcher.apply(metric), fieldName.getScale());
                break;
            default:
                output.put(fieldName.getField(), fetcher.apply(metric));
                break;
        }
    }

    @Override
    protected void writeTimers(String key, MetricSubType metricSubType, SortedMap<String, Timer> timers, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = nameFactory.timerNames(key);
        consumerMetric(map, metricSubType, v -> Optional.ofNullable(timers.get(v.name())).ifPresent(t -> {
                final Snapshot snapshot = t.getSnapshot();
                v.getValueFetcher().forEach((fieldName, fetcher) -> {
                    if (fetcher.getClazz().equals(com.megaease.easeagent.plugin.api.metric.Snapshot.class)) {
                        appendField(output, fieldName, fetcher, SnapshotImpl.build(snapshot));
                    } else {
                        appendField(output, fieldName, fetcher, TimerImpl.build(t));
                    }
                });
            })
        );
    }
}
