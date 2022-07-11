/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.metrics;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.metrics.config.MetricsConfig;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.plugin.report.EncodedData;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class AutoRefreshReporter implements Runnable {
    private final MetricsConfig config;
    private final Converter converter;
    private final Consumer<EncodedData> consumer;
    private final MetricRegistry metricRegistry;
    private AgentScheduledReporter reporter;

    public AutoRefreshReporter(MetricRegistry metricRegistry,
                               MetricsConfig config,
                               Converter converter,
                               Consumer<EncodedData> consumer) {
        this.metricRegistry = metricRegistry;
        this.config = config;
        this.consumer = consumer;
        this.converter = converter;
        config.setIntervalChangeCallback(this);
    }

    @Override
    public synchronized void run() {
        // config changed
        if (reporter != null) {
            reporter.close();
            reporter = null;
        }
        reporter = AgentScheduledReporter.forRegistry(metricRegistry)
            .outputTo(consumer)
            .enabled(config::isEnabled)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
        reporter.setConverter(converter);
        reporter.start(config.getInterval(), config.getIntervalUnit());
    }

    public AgentScheduledReporter getReporter() {
        return reporter;
    }
}
