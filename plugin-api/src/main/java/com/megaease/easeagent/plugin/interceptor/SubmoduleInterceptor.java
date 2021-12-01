/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.interceptor;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.InitializeContext;
import com.megaease.easeagent.plugin.api.config.Config;

public abstract class SubmoduleInterceptor implements Interceptor {
    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
    }

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        Config config = context.getConfig();
        if (!config.enabled()) {
            return;
        }
        InitializeContext  innerContext = (InitializeContext) context;
        innerContext.pushConfig(config);
        doBefore(methodInfo, context);
    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        Config config = context.getConfig();
        if (!config.enabled()) {
            return;
        }
        InitializeContext  innerContext = (InitializeContext) context;
        try {
            doBefore(methodInfo, context);
        } finally {
            innerContext.popConfig();
        }
    }

    public abstract void doInit(Config config, String className, String methodName, String methodDescriptor);
    public abstract void doBefore(MethodInfo methodInfo, Context context);
    public abstract void doAfter(MethodInfo methodInfo, Context context);

    /**
     * submodule name
     * @return submodule name
     */
    public abstract String getSubmoduleName();
}
