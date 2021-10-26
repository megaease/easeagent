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

package com.megaease.easeagent.plugin.api.metric.name;

import java.util.HashMap;
import java.util.Map;


public class MetricName {

    final MetricSubType metricSubType;
    final String key;
    final MetricType metricType;
    final Map<MetricField, MetricValueFetcher> valueFetcher;

    public MetricName(MetricSubType metricSubType, String key, MetricType metricType, Map<MetricField, MetricValueFetcher> valueFetcher) {
        this.metricSubType = metricSubType;
        this.key = key;
        this.metricType = metricType;
        this.valueFetcher = valueFetcher;
    }

    public MetricSubType getMetricSubType() {
        return metricSubType;
    }

    public String getKey() {
        return key;
    }

    public MetricType getMetricType() {
        return metricType;
    }

    public Map<MetricField, MetricValueFetcher> getValueFetcher() {
        return valueFetcher;
    }

    public static MetricName metricNameFor(String name) {
        return new MetricName(
                MetricSubType.valueFor(name.substring(0, 2)),
                name.substring(3),
                MetricType.values()[Integer.parseInt(name.substring(2, 3))],
                new HashMap<>());
    }

    public String name() {
        return metricSubType.getCode() + metricType.ordinal() + key;
    }

}
