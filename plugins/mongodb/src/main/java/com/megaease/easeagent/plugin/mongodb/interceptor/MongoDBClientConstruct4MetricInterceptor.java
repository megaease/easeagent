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
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricSupplier;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.mongodb.MongoDBPlugin;
import com.megaease.easeagent.plugin.mongodb.MongoDBUtils;
import com.megaease.easeagent.plugin.mongodb.points.MongoDBClientConstructPoints;
import com.mongodb.MongoClientSettings;
import com.mongodb.event.CommandListener;

import java.util.List;

import static com.megaease.easeagent.plugin.mongodb.interceptor.InterceptorHelper.mongoClientSettings;


@AdviceTo(value = MongoDBClientConstructPoints.class, plugin = MongoDBPlugin.class)
public class MongoDBClientConstruct4MetricInterceptor implements NonReentrantInterceptor {
    private static volatile MongoDBMetric mongoDBMetric = null;
    private AutoRefreshPluginConfigImpl config;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        this.config = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, "mongodb", this.getType());
        mongoDBMetric = ServiceMetricRegistry.getOrCreate(config, new Tags("application", "mongodb", "operation"), new ServiceMetricSupplier<MongoDBMetric>() {
            @Override
            public NameFactory newNameFactory() {
                return MongoDBMetric.nameFactory();
            }

            @Override
            public MongoDBMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
                return new MongoDBMetric(metricRegistry, nameFactory);
            }
        });
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        MongoClientSettings mongoClientSettings = mongoClientSettings(methodInfo);
        if (mongoClientSettings == null) {
            return;
        }
        List<CommandListener> commandListeners = MongoDBUtils.getFieldValue(mongoClientSettings, "commandListeners");
        commandListeners.add(new MetricMongoDBCommandListener(mongoDBMetric, this.config));
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }
}
