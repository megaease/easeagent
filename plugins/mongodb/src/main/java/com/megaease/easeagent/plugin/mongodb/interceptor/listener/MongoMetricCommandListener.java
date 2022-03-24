/*
 * Copyright (c) 2021 MegaEase
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

package com.megaease.easeagent.plugin.mongodb.interceptor.listener;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.mongodb.interceptor.MetricHelper;
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoMetric;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

public class MongoMetricCommandListener extends MongoBaseMetricCommandListener {

//    private static final Logger LOGGER = LoggerFactory.getLogger(MongoMetricCommandListener.class);

    public MongoMetricCommandListener(AutoRefreshPluginConfigImpl config, MongoMetric mongoMetric) {
        super(config, mongoMetric);
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
//        LOGGER.warn("commandStarted metric");
        Context context = EaseAgent.getOrCreateTracingContext();
        MetricHelper.commandStarted(context, event);
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
//        LOGGER.warn("commandSucceeded metric");
        Context context = EaseAgent.getOrCreateTracingContext();
        MetricHelper.commandSucceeded(context, config, this.mongoMetric, event);
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
//        LOGGER.warn("commandFailed metric");
        Context context = EaseAgent.getOrCreateTracingContext();
        MetricHelper.commandFailed(context, config, this.mongoMetric, event);
    }

}
