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

package com.megaease.easeagent.plugin.mongodb.interceptor;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.mongodb.MongoPlugin;
import com.megaease.easeagent.plugin.mongodb.MongoUtils;
import com.megaease.easeagent.plugin.mongodb.points.MongoDBInternalConnectionPoints;
import com.mongodb.event.CommandEvent;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandSucceededEvent;
import com.mongodb.internal.async.SingleResultCallback;

@AdviceTo(value = MongoDBInternalConnectionPoints.class, plugin = MongoPlugin.class)
public class MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor implements NonReentrantInterceptor {

//    private static final Logger LOGGER = LoggerFactory.getLogger(MongoInternalConnectionSendAndReceiveAsync4MetricInterceptor.class);

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
//        LOGGER.warn("agent: metric - " + methodInfo.getMethod());
        SingleResultCallback<?> callback = (SingleResultCallback<?>) methodInfo.getArgs()[3];
        MongoCtx mongoCtx = MongoCtx.getOrCreate(context);
        SingleResultCallbackProxy<?> proxy = new SingleResultCallbackProxy<>(callback, mongoCtx);
        methodInfo.changeArg(3, proxy);
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    static class SingleResultCallbackProxy<T> implements SingleResultCallback<T> {

//        private static final Logger LOGGER = LoggerFactory.getLogger(SingleResultCallbackProxy.class);

        private final SingleResultCallback<T> delegate;
        private final MongoCtx mongoCtx;

        public SingleResultCallbackProxy(SingleResultCallback<T> delegate, MongoCtx mongoCtx) {
            this.delegate = delegate;
            this.mongoCtx = mongoCtx;
        }

        @Override
        public void onResult(T result, Throwable t) {
//            LOGGER.info("SingleResultCallbackProxy onResult metric");
            this.delegate.onResult(result, t);
            Context context = EaseAgent.getContext();
            CommandEvent event = context.get(MongoUtils.EVENT_KEY);
            if (event == null) {
                return;
            }
            MongoMetric mongoMetric = this.mongoCtx.get(MongoUtils.METRIC);
            AutoRefreshPluginConfigImpl config = this.mongoCtx.get(MongoUtils.CONFIG);
            if (event instanceof CommandSucceededEvent) {
                MetricHelper.commandSucceeded(context, config, mongoMetric, (CommandSucceededEvent) event);
            } else if (event instanceof CommandFailedEvent) {
                MetricHelper.commandFailed(context, config, mongoMetric, (CommandFailedEvent) event);
            }
        }
    }
}
