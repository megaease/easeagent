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

import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.mongodb.interceptor.MongoMetric;

public class MongoBaseMetricCommandListener extends MongoBaseCommandListener {

    protected final MongoMetric mongoMetric;

    public MongoBaseMetricCommandListener(AutoRefreshPluginConfigImpl config, MongoMetric mongoMetric) {
        super(config);
        this.mongoMetric = mongoMetric;
    }

}
