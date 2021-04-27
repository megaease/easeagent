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
