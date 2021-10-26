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

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.common.concurrent.ScheduleHelper;
import com.megaease.easeagent.metrics.converter.Converter;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;

import java.util.Map;
import java.util.function.Supplier;

public abstract class AbstractMetric {

    protected MetricRegistry metricRegistry;

    protected NameFactory nameFactory;

    protected boolean enableSchedule;

    public AbstractMetric(MetricRegistry metricRegistry) {
        this(metricRegistry, false);
    }

    public AbstractMetric(MetricRegistry metricRegistry, boolean enableSchedule) {
        this.metricRegistry = metricRegistry;
        this.enableSchedule = enableSchedule;
        if (this.enableSchedule && this instanceof ScheduleRunner) {
            ScheduleRunner obj = (ScheduleRunner) this;
            ScheduleHelper.DEFAULT.execute(5, 10, obj::doJob);
        }
    }

    public abstract Converter newConverter(Supplier<Map<String, Object>> attributes);
}
