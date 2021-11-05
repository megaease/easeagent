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

package com.megaease.easeagent.plugin.api.metric;

import com.megaease.easeagent.plugin.api.config.Config;
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
     *
     * @param config      {@link Config} metric config
     * @param nameFactory {@link NameFactory} Calculation description and name description of the value of the metric.
     * @param tags        {@link Tags} tags of metric
     * @return
     */
    MetricRegistry newMetricRegistry(Config config, NameFactory nameFactory, Tags tags);
}
