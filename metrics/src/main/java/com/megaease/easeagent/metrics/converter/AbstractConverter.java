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
import com.megaease.easeagent.log4j2.Logger;
import com.megaease.easeagent.log4j2.LoggerFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverter.class);

    private final String rateUnit;
    private final String durationUnit;
    final Long durationFactor;
    final Long rateFactor;
    private final Tags tags;
    private final Supplier<Map<String, Object>> additionalAttributes;

    AbstractConverter(String category, String type, String keyFieldName, Supplier<Map<String, Object>> additionalAttributes) {
        this(additionalAttributes, new Tags(category, type, keyFieldName));
    }

    AbstractConverter(Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
        this.rateFactor = TimeUnit.SECONDS.toSeconds(1);
        this.rateUnit = calculateRateUnit();
        this.durationFactor = TimeUnit.MILLISECONDS.toNanos(1);
        this.durationUnit = TimeUnit.MILLISECONDS.toString().toLowerCase(Locale.US);
        this.additionalAttributes = additionalAttributes;
        this.tags = tags;
    }

    private String calculateRateUnit() {
        final String s = TimeUnit.SECONDS.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }

    @SneakyThrows
    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> convertMap(SortedMap<String, Gauge> gauges,
                                                SortedMap<String, Counter> counters,
                                                SortedMap<String, Histogram> histograms,
                                                SortedMap<String, Meter> meters,
                                                SortedMap<String, Timer> timers) {


        List<String> keys = keysFromMetrics(gauges, counters, histograms, meters, timers);
        final List<Map<String, Object>> result = new ArrayList<>();
        for (String k : keys) {
            try {
                Map<String, Object> output = buildMap();
                writeKey(output, k);
                writeTag(output);
                writeGauges(k, gauges, output);
                writeCounters(k, counters, output);
                writeHistograms(k, histograms, output);
                writeMeters(k, meters, output);
                writeTimers(k, timers, output);
                result.add(output);
            } catch (IgnoreOutputException exception) {
                LOGGER.trace("convert key of " + k + " error: " + exception.getMessage());
            }
        }
        return result;
    }

    private Map<String, Object> buildMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("timestamp", System.currentTimeMillis());
        map.putAll(additionalAttributes.get());
        return map;
    }


    private void writeTag(Map<String, Object> output) {
        output.put(Tags.CATEGORY, tags.getCategory());
        output.put(Tags.TYPE, tags.getType());
        output.putAll(tags.getTags());
    }

    private void writeKey(Map<String, Object> output, String key) {
        output.put(tags.getKeyFieldName(), key);
    }

    @SuppressWarnings("rawtypes")
    protected abstract List<String> keysFromMetrics(SortedMap<String, Gauge> gauges,
                                                    SortedMap<String, Counter> counters,
                                                    SortedMap<String, Histogram> histograms,
                                                    SortedMap<String, Meter> meters,
                                                    SortedMap<String, Timer> timers);


    @SuppressWarnings("rawtypes")
    protected abstract void writeGauges(String key, SortedMap<String, Gauge> gauges, Map<String, Object> output);

    protected abstract void writeCounters(String key, SortedMap<String, Counter> counters, Map<String, Object> output);

    protected abstract void writeHistograms(String key, SortedMap<String, Histogram> histograms, Map<String, Object> output);

    protected abstract void writeMeters(String key, SortedMap<String, Meter> meters, Map<String, Object> output);

    protected abstract void writeTimers(String key, SortedMap<String, Timer> timers, Map<String, Object> output);
}
