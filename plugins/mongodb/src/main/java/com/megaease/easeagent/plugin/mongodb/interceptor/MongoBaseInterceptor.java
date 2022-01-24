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
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.mongodb.MongoUtils;
import com.mongodb.MongoClientSettings;
import com.mongodb.event.CommandListener;

import java.util.List;


public abstract class MongoBaseInterceptor implements NonReentrantInterceptor {

    protected AutoRefreshPluginConfigImpl config;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        this.config = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, "mongodb", this.getType());
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        MongoClientSettings mongoClientSettings = MongoUtils.mongoClientSettings(methodInfo);
        if (mongoClientSettings == null) {
            return;
        }
        List<CommandListener> commandListeners = MongoUtils.getFieldValue(mongoClientSettings, "commandListeners");
        commandListeners.add(this.commandListener());
    }

    protected abstract CommandListener commandListener();
}
