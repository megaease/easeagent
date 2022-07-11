/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.mongodb.interceptor;


import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.mongodb.MongoPlugin;
import com.megaease.easeagent.plugin.mongodb.interceptor.listener.MongoReactiveMetricCommandListener;
import com.megaease.easeagent.plugin.mongodb.points.MongoAsyncMongoClientsPoints;
import com.mongodb.event.CommandListener;

@AdviceTo(value = MongoAsyncMongoClientsPoints.class, plugin = MongoPlugin.class)
public class MongoReactiveInitMetricInterceptor extends MongoBaseMetricInterceptor {

    @Override
    protected CommandListener commandListener() {
        return new MongoReactiveMetricCommandListener(this.config, this.mongoMetric);
    }
}
