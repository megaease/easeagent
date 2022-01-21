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
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoCtx;
import com.megaease.easeagent.plugin.mongodb.interceptor.TraceHelper;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;

public class MongoReactiveTraceCommandListener extends MongoBaseTraceCommandListener {

//    private static final Logger LOGGER = LoggerFactory.getLogger(MongoReactiveTraceCommandListener.class);

    public MongoReactiveTraceCommandListener(AutoRefreshPluginConfigImpl config) {
        super(config);
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
//        LOGGER.warn("reactive commandStarted trace");
        Context context = EaseAgent.getContext();
        TraceHelper.commandStarted(context, this.config, event);
        MongoCtx mongoCtx = MongoCtx.getOrCreate(context);
        mongoCtx.put(InterceptorHelper.CONFIG, this.config);
        mongoCtx.put(TraceHelper.SPAN_KEY, context.get(TraceHelper.SPAN_KEY));
    }


    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
//        LOGGER.warn("reactive commandSucceeded trace");
        Context context = EaseAgent.getContext();
        context.put(InterceptorHelper.EVENT_KEY, event);
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
//        LOGGER.warn("reactive commandFailed trace");
        Context context = EaseAgent.getContext();
        context.put(InterceptorHelper.EVENT_KEY, event);
    }

}
