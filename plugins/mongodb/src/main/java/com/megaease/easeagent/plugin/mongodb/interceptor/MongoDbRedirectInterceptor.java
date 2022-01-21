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
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.mongodb.MongoRedirectPlugin;
import com.megaease.easeagent.plugin.mongodb.points.MongoRedirectPoints;

@AdviceTo(value = MongoRedirectPoints.class, plugin = MongoRedirectPlugin.class)
public class MongoDbRedirectInterceptor implements Interceptor {

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        Interceptor.super.init(config, className, methodName, methodDescriptor);
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.MONGODB.getConfig();
        if (cnf == null) {
            return;
        }
        methodInfo.changeArg(0, cnf.getFirstUri());
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }


}
