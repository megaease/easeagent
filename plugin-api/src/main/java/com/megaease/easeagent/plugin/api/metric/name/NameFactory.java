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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface NameFactory {

    static Builder createBuilder() {
        return new Builder();
    }

    Map<MetricSubType, MetricName> meterNames(String key);

    Map<MetricSubType, MetricName> histogramNames(String key);

    Map<MetricSubType, MetricName> counterNames(String key);

    Map<MetricSubType, MetricName> timerNames(String key);

    Map<MetricSubType, MetricName> gaugeNames(String key);

    String meterName(String key, MetricSubType subType);

    String histogramName(String key, MetricSubType subType);

    String counterName(String key, MetricSubType subType);

    String timerName(String key, MetricSubType subType);

    String gaugeName(String key, MetricSubType subType);

    class DefaultNameFactory implements NameFactory {

        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> histogramTypes;
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> counterTypes;
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> timerTypes;
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> gaugeTypes;
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> meterTypes;


        private DefaultNameFactory(List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> meterTypes,
                                   List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> histogramTypes,
                                   List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> counterTypes,
                                   List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> timerTypes,
                                   List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> gaugeTypes) {
            this.meterTypes = meterTypes;
            this.histogramTypes = histogramTypes;
            this.counterTypes = counterTypes;
            this.timerTypes = timerTypes;
            this.gaugeTypes = gaugeTypes;
        }

        @Override
        public Map<MetricSubType, MetricName> meterNames(String key) {
            final Map<MetricSubType, MetricName> results = new HashMap<>();
            meterTypes.forEach(t -> results.put(t.getX(),
                new MetricName(t.getX(), key, MetricType.MeterType, t.getY())));
            return results;
        }

        @Override
        public String meterName(String key, MetricSubType subType) {
            return getName(key, MetricType.MeterType, subType, meterTypes);
        }

        @Override
        public Map<MetricSubType, MetricName> histogramNames(String key) {
            final Map<MetricSubType, MetricName> results = new HashMap<>();
            histogramTypes.forEach(t -> results.put(t.getX(),
                new MetricName(t.getX(), key, MetricType.HistogramType, t.getY())));
            return results;
        }

        @Override
        public String histogramName(String key, MetricSubType subType) {
            return getName(key, MetricType.HistogramType, subType, histogramTypes);
        }

        @Override
        public Map<MetricSubType, MetricName> counterNames(String key) {
            final Map<MetricSubType, MetricName> results = new HashMap<>();
            counterTypes.forEach(t -> results.put(t.getX(),
                new MetricName(t.getX(), key, MetricType.CounterType, t.getY())));
            return results;
        }

        @Override
        public String counterName(String key, MetricSubType subType) {
            return getName(key, MetricType.CounterType, subType, counterTypes);
        }


        @Override
        public Map<MetricSubType, MetricName> timerNames(String key) {
            final Map<MetricSubType, MetricName> results = new HashMap<>();
            timerTypes.forEach(t -> results.put(t.getX(),
                new MetricName(t.getX(), key, MetricType.TimerType, t.getY())));
            return results;
        }

        @Override
        public String timerName(String key, MetricSubType subType) {
            return getName(key, MetricType.TimerType, subType, timerTypes);
        }

        @Override
        public Map<MetricSubType, MetricName> gaugeNames(String key) {
            final Map<MetricSubType, MetricName> results = new HashMap<>();
            gaugeTypes.forEach(t -> results.put(t.getX(),
                new MetricName(t.getX(), key, MetricType.GaugeType, t.getY())));
            return results;
        }

        @Override
        public String gaugeName(String key, MetricSubType metricSubType) {
            return getName(key, MetricType.GaugeType, metricSubType, gaugeTypes);
        }

        private String getName(String key, MetricType metricType, MetricSubType metricSubType, List<Tuple<MetricSubType,
            Map<MetricField, MetricValueFetcher>>> metricsTypes) {
            MetricName metricName = null;
            for (Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>> t : metricsTypes) {
                if (t.getX().equals(metricSubType)) {
                    metricName = new MetricName(t.getX(), key, metricType, t.getY());
                }
            }
            if (metricName == null) {
                throw new IllegalArgumentException("Invalid metricSubType [" + metricSubType.name() + "] of " + metricType.name() +
                    " not be registered in NameFactory");
            }
            return metricName.name();
        }
    }


    class Tuple<X, Y> {
        private X x;
        private Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        public X getX() {
            return x;
        }

        public void setX(X x) {
            this.x = x;
        }

        public Y getY() {
            return y;
        }

        public void setY(Y y) {
            this.y = y;
        }
    }


    class Builder {
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> histogramTypes = new ArrayList<>();
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> counterTypes = new ArrayList<>();
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> timerTypes = new ArrayList<>();
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> gaugeTypes = new ArrayList<>();
        private final List<Tuple<MetricSubType, Map<MetricField, MetricValueFetcher>>> meterTypes = new ArrayList<>();

        Builder() {
        }

        public NameFactory build() {
            return new DefaultNameFactory(meterTypes, histogramTypes, counterTypes, timerTypes, gaugeTypes);
        }

        public Builder meterType(MetricSubType metricSubType, Map<MetricField, MetricValueFetcher> valueFetchers) {
            meterTypes.add(new Tuple<>(metricSubType, valueFetchers));
            return this;
        }

        public Builder histogramType(MetricSubType metricSubType, Map<MetricField, MetricValueFetcher> valueFetchers) {
            histogramTypes.add(new Tuple<>(metricSubType, valueFetchers));
            return this;
        }

        public Builder counterType(MetricSubType metricSubType, Map<MetricField, MetricValueFetcher> valueFetchers) {
            counterTypes.add(new Tuple<>(metricSubType, valueFetchers));
            return this;
        }

        public Builder timerType(MetricSubType metricSubType, Map<MetricField, MetricValueFetcher> valueFetchers) {
            this.timerTypes.add(new Tuple<>(metricSubType, valueFetchers));
            return this;
        }

        public Builder gaugeType(MetricSubType metricSubType, Map<MetricField, MetricValueFetcher> valueFetchers) {
            this.gaugeTypes.add(new Tuple<>(metricSubType, valueFetchers));
            return this;
        }
    }
}
