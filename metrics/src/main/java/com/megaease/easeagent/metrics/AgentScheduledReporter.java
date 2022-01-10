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

package com.megaease.easeagent.metrics;

import com.codahale.metrics.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.megaease.easeagent.metrics.converter.Converter;
import lombok.SneakyThrows;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class AgentScheduledReporter extends ScheduledReporter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private Converter converter;
    private final Consumer<String> dataConsumer;
    private final Supplier<Boolean> enabled;

    @SuppressWarnings("all")
    private AgentScheduledReporter(MetricRegistry registry,
                                   Consumer<String> dataConsumer,
                                   TimeUnit rateUnit,
                                   TimeUnit durationUnit,
                                   MetricFilter filter,
                                   ScheduledExecutorService executor,
                                   boolean shutdownExecutorOnStop,
                                   Set<MetricAttribute> disabledMetricAttributes,
                                   Supplier<Boolean> enabled,
                                   Converter converter) {
        super(registry, "logger-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop,
            disabledMetricAttributes);
        this.converter = converter;
        this.dataConsumer = dataConsumer;
        this.enabled = enabled;
    }

    /**
     * Returns a new {@link Slf4jReporter.Builder} for {@link Slf4jReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Slf4jReporter.Builder} instance for a {@link Slf4jReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }


    @SneakyThrows
    @Override
    @SuppressWarnings("rawtypes")
    public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, com.codahale.metrics.Timer> timers) {
        Boolean e = this.enabled.get();
        if (e != null && !e.booleanValue()) {
            return;
        }

        List<Map<String, Object>> outputs = converter.convertMap(gauges, counters, histograms, meters, timers);
        for (Map<String, Object> output : outputs) {
            this.dataConsumer.accept(objectMapper.writeValueAsString(output));
        }
    }


    @Override
    protected String getRateUnit() {
        return "events/" + super.getRateUnit();
    }


    public Converter getConverter() {
        return this.converter;
    }

    /**
     * Invoke it in the Metric constructor only.
     *
     * @param converter convert meter to List<Map<String, Object>> to serialize to json
     */
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    /**
     * A builder for {@link Slf4jReporter} instances. Defaults to logging to {@code metrics}, not
     * using a marker, converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;
        private ScheduledExecutorService executor;
        private boolean shutdownExecutorOnStop;
        private Set<MetricAttribute> disabledMetricAttributes;
        private Converter converter;
        private Supplier<Boolean> enabled;
        private Consumer<String> dataConsumer;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.executor = null;
            this.shutdownExecutorOnStop = true;
            this.disabledMetricAttributes = Collections.emptySet();
        }

        /**
         * Specifies whether or not, the executor (used for reporting) will be stopped with same time with reporter.
         * Default value is true.
         * Setting this parameter to false, has the sense in combining with providing external managed executor via {@link #scheduleOn(ScheduledExecutorService)}.
         *
         * @param shutdownExecutorOnStop if true, then executor will be stopped in same time with this reporter
         * @return {@code this}
         */
        public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
            this.shutdownExecutorOnStop = shutdownExecutorOnStop;
            return this;
        }

        /**
         * Specifies the executor to use while scheduling reporting of metrics.
         * Default value is null.
         * Null value leads to executor will be auto created on start.
         *
         * @param executor the executor to use while scheduling reporting of metrics.
         * @return {@code this}
         */
        public Builder scheduleOn(ScheduledExecutorService executor) {
            this.executor = executor;
            return this;
        }

        public Builder converter(Converter converter) {
            this.converter = converter;
            return this;
        }

        /**
         * Log metrics to the given consumer.
         *
         * @return {@code this}
         */
        public Builder outputTo(Consumer<String> dataConsumer) {
            this.dataConsumer = dataConsumer;
            return this;
        }

        public Builder enabled(Supplier<Boolean> enabled) {
            this.enabled = enabled;
            return this;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }

        /**
         * Don't report the passed metric attributes for all metrics (e.g. "p999", "stddev" or "m15").
         * See {@link MetricAttribute}.
         *
         * @param disabledMetricAttributes a set of {@link MetricAttribute}
         * @return {@code this}
         */
        public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
            this.disabledMetricAttributes = disabledMetricAttributes;
            return this;
        }

        /**
         * Builds a {@link Slf4jReporter} with the given properties.
         *
         * @return a {@link Slf4jReporter}
         */
        public AgentScheduledReporter build() {

            return new AgentScheduledReporter(registry, dataConsumer, rateUnit,
                durationUnit, filter, executor, shutdownExecutorOnStop,
                disabledMetricAttributes, enabled, converter);
        }

    }
}
