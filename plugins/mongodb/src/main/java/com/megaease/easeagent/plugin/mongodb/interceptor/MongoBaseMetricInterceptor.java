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

import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricSupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.enums.Order;


public abstract class MongoBaseMetricInterceptor extends MongoBaseInterceptor {
    protected MongoMetric mongoMetric = null;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        super.init(config, className, methodName, methodDescriptor);
        Tags tags = new Tags("application", "mongodb", "operation");
        RedirectProcessor.setTagsIfRedirected(Redirect.MONGODB, tags);
        mongoMetric = ServiceMetricRegistry.getOrCreate(config, tags, new ServiceMetricSupplier<MongoMetric>() {
            @Override
            public NameFactory newNameFactory() {
                return MongoMetric.nameFactory();
            }

            @Override
            public MongoMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
                return new MongoMetric(metricRegistry, nameFactory);
            }
        });
    }

    public MongoMetric getMongoMetric() {
        return mongoMetric;
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }
}
