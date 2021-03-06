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

import com.codahale.metrics.Timer;
import com.codahale.metrics.*;
import com.megaease.easeagent.metrics.MetricField;
import com.megaease.easeagent.metrics.MetricName;
import com.megaease.easeagent.metrics.MetricNameFactory;
import com.megaease.easeagent.metrics.MetricSubType;
import com.megaease.easeagent.metrics.model.GaugeMetricModel;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Supplier;

public class ConverterAdapter extends AbstractConverter {

    private final KeyType keyType;

    private final MetricNameFactory metricNameFactory;

    public ConverterAdapter(String category, String type, MetricNameFactory metricNameFactory, KeyType keyType,
                            Supplier<Map<String, Object>> attributes, String keyFieldName) {
        super(category, type, keyFieldName, attributes);
        this.keyType = keyType;
        this.metricNameFactory = metricNameFactory;
    }

    public ConverterAdapter(String category, String type, MetricNameFactory metricNameFactory, KeyType keyType,
                            Supplier<Map<String, Object>> attributes) {
        this(category, type, metricNameFactory, keyType, attributes, "resource");
    }


    @Override
    protected List<String> keysFromMetrics(SortedMap<String, Gauge> gauges,
                                           SortedMap<String, Counter> counters,
                                           SortedMap<String, Histogram> histograms,
                                           SortedMap<String, Meter> meters,
                                           SortedMap<String, Timer> timers) {
        switch (keyType) {
            case Timer:
                return keysFromTimer(timers);
            case Histogram:
                return KeysFromHistograms(histograms);
            case Gauge:
                return KeysFromGauges(gauges);
            case Counter:
                return KeysFromCounters(counters);
            case Meter:
                return KeysFromMeters(meters);
        }

        return null;
    }

    private List<String> KeysFromHistograms(SortedMap<String, Histogram> histograms) {
        return keys(histograms.keySet());
    }

    private List<String> KeysFromGauges(SortedMap<String, Gauge> gauges) {
        return keys(gauges.keySet());
    }

    private List<String> KeysFromCounters(SortedMap<String, Counter> counters) {
        return keys(counters.keySet());
    }

    private List<String> KeysFromMeters(SortedMap<String, Meter> meters) {
        return keys(meters.keySet());
    }

    private List<String> keysFromTimer(SortedMap<String, Timer> timers) {
        return keys(timers.keySet());
    }

    private List<String> keys(Set<String> origins) {
        final Set<String> results = new HashSet<>();
        origins.forEach(s -> results.add(MetricName.metricNameFor(s).getKey()));
        return new ArrayList<>(results);
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
        return rate * rateFactor;
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
    protected void writeGauges(String key, SortedMap<String, Gauge> gauges, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = metricNameFactory.gaugeNames(key);
        map.values().forEach(v -> {
            Gauge gauge = gauges.get(v.name());
            if (gauge == null) {
                return;
            }
            Object value = gauge.getValue();
            if (value instanceof GaugeMetricModel) {
                GaugeMetricModel model = (GaugeMetricModel) value;
                output.putAll(model.toHashMap());
            }
        });
    }

    @Override
    protected void writeCounters(String key, SortedMap<String, Counter> counters, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = metricNameFactory.counterNames(key);
        map.values().forEach(v -> Optional
                .ofNullable(counters.get(v.name()))
                .ifPresent(c -> v.getValueFetcher().forEach((fieldName, fetcher) -> {
                    // for test
                    appendField(output, fieldName, fetcher, c);
                })));

    }

    @Override
    protected void writeHistograms(String key, SortedMap<String, Histogram> histograms, Map<String, Object> output) {

    }

    @Override
    protected void writeMeters(String key, SortedMap<String, Meter> meters, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = metricNameFactory.meterNames(key);
        map.values().forEach(v -> Optional
                .ofNullable(meters.get(v.name()))
                .ifPresent(m -> v.getValueFetcher().forEach(
                        (fieldName, fetcher) -> {
                            // for test
                            appendField(output, fieldName, fetcher, m);
                        }))
        );
    }

    private void appendField(Map<String, Object> output, MetricField fieldName, MetricValueFetcher fetcher,
                             Object object) {
        switch (fieldName.getType()) {
            case DURATION:
                appendDuration(output, fieldName.getField(), fetcher.apply(object), fieldName.getScale());
                break;
            case RATE:
                appendRate(output, fieldName.getField(), fetcher.apply(object), fieldName.getScale());
                break;
            default:
                output.put(fieldName.getField(), fetcher.apply(object));
                break;
        }
    }

    @Override
    protected void writeTimers(String key, SortedMap<String, Timer> timers, Map<String, Object> output) {
        Map<MetricSubType, MetricName> map = metricNameFactory.timerNames(key);
        map.values().forEach(v -> Optional.ofNullable(timers.get(v.name())).ifPresent(t -> {
                    final Snapshot snapshot = t.getSnapshot();
                    v.getValueFetcher().forEach((fieldName, fetcher) -> {
                        if (fetcher.clazz.equals(Snapshot.class)) {
                            appendField(output, fieldName, fetcher, snapshot);
                        } else {
                            appendField(output, fieldName, fetcher, t);
                        }
                    });
                })
        );
    }
}
