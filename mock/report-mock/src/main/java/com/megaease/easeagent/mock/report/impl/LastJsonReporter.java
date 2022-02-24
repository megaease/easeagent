/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.mock.report.impl;

import com.megaease.easeagent.mock.report.JsonReporter;
import com.megaease.easeagent.mock.report.MetricFlushable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class LastJsonReporter implements JsonReporter {
    private final MetricFlushable metricFlushable;
    private final AtomicReference<List<Map<String, Object>>> reference = new AtomicReference<>();
    private final Predicate<Map<String, Object>> filter;

    public LastJsonReporter(Predicate<Map<String, Object>> filter, MetricFlushable metricFlushable) {
        this.filter = filter;
        this.metricFlushable = metricFlushable;
    }

    @Override
    public void report(List<Map<String, Object>> json) {
        if (filter == null) {
            reference.set(json);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> stringObjectMap : json) {
            if (filter.test(stringObjectMap)) {
                result.add(stringObjectMap);
            }
        }
        if (!result.isEmpty()) {
            reference.set(result);
        }
    }

    public Map<String, Object> flushAndOnlyOne() {
        List<Map<String, Object>> metrics = flushAndGet();
        if (metrics.size() != 1) {
            throw new RuntimeException("metrics size is not 1 ");
        }
        return metrics.get(0);
    }

    public List<Map<String, Object>> flushAndGet() {
        metricFlushable.flush();
        List<Map<String, Object>> metric = reference.get();
        if (metric == null || metric.isEmpty()) {
            throw new RuntimeException("metric must not be null and empty.");
        }
        return metric;
    }

    public void clean() {
        reference.set(null);
    }
}
