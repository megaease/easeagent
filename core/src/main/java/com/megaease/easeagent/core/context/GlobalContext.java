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

package com.megaease.easeagent.core.context;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.plugin.api.logging.ILoggerFactory;
import com.megaease.easeagent.plugin.api.logging.Mdc;
import com.megaease.easeagent.plugin.api.metric.MetricRegistrySupplier;

import javax.annotation.Nonnull;

public class GlobalContext {
    private final Configs conf;
    private final MetricRegistrySupplier metric;
    private final ILoggerFactory loggerFactory;
    private final Mdc mdc;

    public GlobalContext(@Nonnull Configs conf, @Nonnull MetricRegistrySupplier metric, @Nonnull ILoggerFactory loggerFactory, @Nonnull Mdc mdc) {
        this.conf = conf;
        this.metric = metric;
        this.loggerFactory = loggerFactory;
        this.mdc = mdc;
    }


    public Configs getConf() {
        return conf;
    }

    public Mdc getMdc() {
        return mdc;
    }


    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public MetricRegistrySupplier getMetric() {
        return metric;
    }
}
