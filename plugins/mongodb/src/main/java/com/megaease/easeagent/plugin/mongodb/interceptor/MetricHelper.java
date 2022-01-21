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

package com.megaease.easeagent.plugin.mongodb.interceptor;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.mongodb.event.CommandFailedEvent;
import com.mongodb.event.CommandStartedEvent;
import com.mongodb.event.CommandSucceededEvent;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class MetricHelper {

    private static final String START_TIME = MetricHelper.class.getName() + "-StartTime";

    public static void commandStarted(Context context, CommandStartedEvent event) {
        context.put(START_TIME, System.currentTimeMillis());
    }

    private static void processAfter(Context context, AutoRefreshPluginConfigImpl config,
                                     MongoMetric mongoMetric, String key, boolean success) {
        if (!config.getConfig().enabled()) {
            return;
        }
        long startTime = context.get(START_TIME);
        long duration = System.currentTimeMillis() - startTime;
        mongoMetric.collectMetric(key, duration, success);
    }
//
//    public static void processAfter(Context context, AutoRefreshPluginConfigImpl config,
//                                    MongoMetric mongoMetric,
//                                    CommandSucceededEvent commandSucceededEvent,
//                                    CommandFailedEvent commandFailedEvent
//    ) {
//        if (commandSucceededEvent != null) {
//            BsonDocument bsonDocument = commandSucceededEvent.getResponse();
//            BsonValue writeErrors = bsonDocument.get("writeErrors");
//            boolean success = writeErrors == null;
//            MetricHelper.processAfter(context, config, mongoMetric, commandSucceededEvent.getCommandName(), success);
//        } else if (commandFailedEvent != null) {
//            MetricHelper.processAfter(context, config, mongoMetric, commandFailedEvent.getCommandName(), false);
//        }
//    }

    public static void commandSucceeded(Context context, AutoRefreshPluginConfigImpl config,
                                        MongoMetric mongoMetric,
                                        CommandSucceededEvent commandSucceededEvent
    ) {
        BsonDocument bsonDocument = commandSucceededEvent.getResponse();
        BsonValue writeErrors = bsonDocument.get("writeErrors");
        boolean success = writeErrors == null;
        MetricHelper.processAfter(context, config, mongoMetric, commandSucceededEvent.getCommandName(), success);
    }

    public static void commandFailed(Context context, AutoRefreshPluginConfigImpl config,
                                     MongoMetric mongoMetric,
                                     CommandFailedEvent commandFailedEvent
    ) {
        MetricHelper.processAfter(context, config, mongoMetric, commandFailedEvent.getCommandName(), false);
    }
}
