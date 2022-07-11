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

package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.Reporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;

/**
 * A supplier of MetricRegistry interface.
 */
public interface MetricRegistrySupplier {
    /**
     * new and return a MetricRegistry for.
     * Use configure report output:
     * <pre>{@code
     *  config.getBoolean("enabled")
     *  config.getInt("interval")
     *  config.getString("topic")
     *  config.getString("appendType")
     * }</pre>
     * <p>
     * <p>
     * In the metric example here, all metrics are output in the form of json.
     * All {@code tags} will also be placed in the json text in the form of key:value.
     * <p>
     * Different metric types have different values. The same metric will also have different values.
     * for example:
     * Timer can calculate count, avg, max and so on.
     * What type and what value is calculated? What field is this value stored in json?
     * In this method, the {@code nameFactory} is used for control.
     * We have implemented some commonly used content by default, or we can customize our own content.
     *
     * <pre>{@code
     * NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.DEFAULT,
     *              ImmutableMap.<MetricField, MetricValueFetcher>builder()
     *             .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
     *             .build()).build();
     * MetricRegistry metricRegistry = supplier.newMetricRegistry(config, nameFactory, new Tags("application", "http-request", "url"));
     * metricRegistry.counter(nameFactory.counterName("http://127.0.0.1:8080", MetricSubType.DEFAULT)).inc();
     * }</pre>
     * The above code tells the calculation program:
     * Need a Counter, this Counter calculates the value of {@link MetricField#EXECUTION_COUNT}(key="cnt"},
     * this value is obtained using the {@link Counter#getCount}  method
     * <p>
     * The output is as follows:
     * <pre>{@code
     *     {
     *         "category": "application",
     *         "type": "http-request",
     *         "url": "http://127.0.0.1:8080",
     *         "cnt": 1
     *     }
     * }</pre>
     *
     * @param config      {@link IPluginConfig} metric config
     * @param nameFactory {@link NameFactory} Calculation description and name description of the value of the metric.
     * @param tags        {@link Tags} tags of metric
     * @return {@link MetricRegistry}
     */
    MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags);

    /**
     * get plugin metric reporter
     *
     * <pre>{@code
     *     Reporter reporter = supplier.reporter(config);
     *     reporter.report("{'url': 'http://127.0.0.1:8080', 'cnt': 1}");
     * }
     * @param config {@link IPluginConfig} metric config
     * @return {@link Reporter}
     */
    Reporter reporter(IPluginConfig config);
}
