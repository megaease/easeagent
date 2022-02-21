/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.mock.plugin.api.utils;

import com.megaease.easeagent.plugin.AgentPlugin;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.Interceptor;

public class InterceptorTestUtils {
    public static void initByUnique(Interceptor interceptor, AgentPlugin agentPlugin) {
        IPluginConfig iPluginConfig = EaseAgent.getConfig(agentPlugin.getDomain(), agentPlugin.getNamespace(), interceptor.getType());
        interceptor.init(iPluginConfig, 0);
    }

    public static void init(Interceptor interceptor, AgentPlugin agentPlugin) {
        init(interceptor, agentPlugin, "", "", "");
    }

    public static void init(Interceptor interceptor, AgentPlugin agentPlugin, String className, String methodName, String methodDescriptor) {
        IPluginConfig iPluginConfig = EaseAgent.getConfig(agentPlugin.getDomain(), agentPlugin.getNamespace(), interceptor.getType());
        interceptor.init(iPluginConfig, className, methodName, methodDescriptor);
    }
}
