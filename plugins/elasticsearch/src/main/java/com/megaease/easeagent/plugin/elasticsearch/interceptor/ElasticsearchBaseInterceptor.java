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

package com.megaease.easeagent.plugin.elasticsearch.interceptor;

import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigImpl;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

public abstract class ElasticsearchBaseInterceptor implements Interceptor {

    protected AutoRefreshPluginConfigImpl config;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        this.config = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY, ConfigConst.Namespace.ELASTICSEARCH, this.getType());
    }

}
