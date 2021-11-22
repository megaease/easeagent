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

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.bridge.EaseAgent;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public abstract class AbstractMetric {
    public static final ConcurrentHashMap<String, AbstractMetric> INSTANCES = new ConcurrentHashMap<>();
    protected final MetricRegistry metricRegistry;
    protected final NameFactory nameFactory;

    protected AbstractMetric(@Nonnull Config config, @Nonnull Tags tags) {
        this.nameFactory = nameFactory();
        this.metricRegistry = EaseAgent.newMetricRegistry(config, nameFactory, tags);
    }

    @Nonnull
    protected abstract NameFactory nameFactory();

    public static <T extends AbstractMetric> T getInstance(Config config, Tags tags, BiFunction<Config, Tags, T> builder) {
        String key = String.format("%s_%s_%s_%s_%s_%s", config.domain(), config.namespace(), config.id(), tags.getCategory(), tags.getType(), tags.getKeyFieldName());
        AbstractMetric abstractMetric = INSTANCES.get(key);
        if (abstractMetric != null) {
            return (T) abstractMetric;
        }
        synchronized (INSTANCES) {
            abstractMetric = INSTANCES.get(key);
            if (abstractMetric != null) {
                return (T) abstractMetric;
            }
            abstractMetric = builder.apply(config, tags);
            INSTANCES.put(key, abstractMetric);
        }
        return (T) abstractMetric;
    }
}
