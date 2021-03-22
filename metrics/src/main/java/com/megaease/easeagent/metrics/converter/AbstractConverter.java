package com.megaease.easeagent.metrics.converter;

import com.codahale.metrics.Timer;
import com.codahale.metrics.*;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class AbstractConverter implements Converter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverter.class);

    private static final String CATEGORY = "category";
    private static final String TYPE = "type";
    protected final String category;
    protected final String type;
    private final String rateUnit;
    private final String durationUnit;
    final Long durationFactor;
    final Long rateFactor;
    private final String keyFieldName;
    private final Supplier<Map<String, Object>> additionalAttributes;

    AbstractConverter(String category, String type, String keyFieldName, Supplier<Map<String, Object>> additionalAttributes) {
        this.category = category;
        this.type = type;
        this.rateFactor = TimeUnit.SECONDS.toSeconds(1);
        this.rateUnit = calculateRateUnit();
        this.durationFactor = TimeUnit.MILLISECONDS.toNanos(1);
        this.durationUnit = TimeUnit.MILLISECONDS.toString().toLowerCase(Locale.US);
        this.keyFieldName = keyFieldName;
        this.additionalAttributes = additionalAttributes;
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
                writerCategoryAndType(output);
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

    private void writeKey(Map<String, Object> output, String key) {
        output.put(keyFieldName, key);
    }

    @SuppressWarnings("rawtypes")
    protected abstract List<String> keysFromMetrics(SortedMap<String, Gauge> gauges,
                                                    SortedMap<String, Counter> counters,
                                                    SortedMap<String, Histogram> histograms,
                                                    SortedMap<String, Meter> meters,
                                                    SortedMap<String, Timer> timers);

    private void writerCategoryAndType(Map<String, Object> output) {
        output.put(CATEGORY, category);
        output.put(TYPE, type);
    }


    @SuppressWarnings("rawtypes")
    protected abstract void writeGauges(String key, SortedMap<String, Gauge> gauges, Map<String, Object> output);

    protected abstract void writeCounters(String key, SortedMap<String, Counter> counters, Map<String, Object> output);

    protected abstract void writeHistograms(String key, SortedMap<String, Histogram> histograms, Map<String, Object> output);

    protected abstract void writeMeters(String key, SortedMap<String, Meter> meters, Map<String, Object> output);

    protected abstract void writeTimers(String key, SortedMap<String, Timer> timers, Map<String, Object> output);
}
