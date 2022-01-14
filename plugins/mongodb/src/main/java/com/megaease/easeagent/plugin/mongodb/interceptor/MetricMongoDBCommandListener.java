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

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandListener;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class MetricMongoDBCommandListener implements CommandListener {

    private static final String START_TIME = MetricMongoDBCommandListener.class.getName() + "-StartTime";

    private final MongoDBMetric mongoDBMetric;
    private final AutoRefreshPluginConfigImpl config;

    public MetricMongoDBCommandListener(MongoDBMetric mongoDBMetric, AutoRefreshPluginConfigImpl config) {
        this.mongoDBMetric = mongoDBMetric;
        this.config = config;
    }

    @Override
    public void commandStarted(CommandStartedEvent event) {
        Context context = EaseAgent.contextSupplier.get();
        context.put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void commandSucceeded(CommandSucceededEvent event) {
        BsonDocument bsonDocument = event.getResponse();
        BsonValue writeErrors = bsonDocument.get("writeErrors");
        boolean success = writeErrors == null;
        this.process(event.getCommandName(), success);
    }

    @Override
    public void commandFailed(CommandFailedEvent event) {
        this.process(event.getCommandName(), false);
    }

    private void process(String key, boolean success) {
        if (!this.config.getConfig().enabled()) {
            return;
        }
        Context context = EaseAgent.contextSupplier.get();
        long startTime = context.get(START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        this.mongoDBMetric.collectMetric(key, duration, success);
    }
}
