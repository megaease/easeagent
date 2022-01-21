/*
 *
 *  * Copyright (c) 2017, MegaEase
 *  * All rights reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.megaease.easeagent.plugin.mongodb.interceptor.listener;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.mongodb.interceptor.InterceptorHelper;
import com.megaease.easeagent.plugin.mongodb.interceptor.MetricHelper;
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoCtx;
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoMetric;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

public class MongoReactiveMetricCommandListener extends MongoBaseMetricCommandListener {

//    private static final Logger LOGGER = LoggerFactory.getLogger(MongoReactiveMetricCommandListener.class);

    public MongoReactiveMetricCommandListener(AutoRefreshPluginConfigImpl config, MongoMetric mongoMetric) {
        super(config, mongoMetric);
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
//        LOGGER.warn("reactive commandStarted metric");
        Context context = EaseAgent.getContext();
        MongoCtx mongoCtx = MongoCtx.getOrCreate(context);
        MetricHelper.commandStarted(context, event);
        mongoCtx.put(InterceptorHelper.METRIC, this.mongoMetric);
        mongoCtx.put(InterceptorHelper.CONFIG, this.config);
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
//        LOGGER.warn("reactive commandSucceeded metric");
        Context context = EaseAgent.getContext();
        context.put(InterceptorHelper.EVENT_KEY, event);
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
//        LOGGER.warn("reactive commandFailed metric");
        Context context = EaseAgent.getContext();
        context.put(InterceptorHelper.EVENT_KEY, event);
    }

}
