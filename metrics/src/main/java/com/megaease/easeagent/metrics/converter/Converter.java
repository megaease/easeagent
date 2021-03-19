package com.megaease.easeagent.metrics.converter;

import com.codahale.metrics.*;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * Converter is dedicated to converting metrics object to a
 * serializable <b>HashMap</b> according to * metric scheme
 * definition
 */
public interface Converter {
    @SuppressWarnings("rawtypes")
    List<Map<String, Object>> convertMap(SortedMap<String, Gauge> gauges,
                                         SortedMap<String, Counter> counters,
                                         SortedMap<String, Histogram> histograms,
                                         SortedMap<String, Meter> meters,
                                         SortedMap<String, Timer> timers);
}
