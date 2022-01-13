/*
 * Copyright (c) 2017, MegaEase
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

package com.megaease.easeagent.plugin.servicename.interceptor;

import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.servicename.ServiceNamePluginConfig;

public abstract class BaseServiceNameInterceptor implements Interceptor {
    protected static ServiceNamePluginConfig config = null;

    @Override
    public void init(IPluginConfig pConfig, String className, String methodName, String methodDescriptor) {
        config = AutoRefreshPluginConfigRegistry.getOrCreate(pConfig.domain(), pConfig.namespace(), pConfig.id(), ServiceNamePluginConfig.SUPPLIER);
    }

    @Override
    public int order() {
        return Order.HIGH.getOrder();
    }

    @Override
    public String getType() {
        return "addServiceNameHead";
    }
}
