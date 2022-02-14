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

package com.megaease.easeagent.plugin.servicename.interceptor;

import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.servicename.Const;
import com.megaease.easeagent.plugin.servicename.ServiceNamePlugin;
import com.megaease.easeagent.plugin.servicename.ServiceNamePluginConfig;
import org.junit.Test;

import static org.junit.Assert.*;

public class BaseServiceNameInterceptorTest {

    public static void initInterceptor(Interceptor interceptor) {
        ServiceNamePlugin plugin = new ServiceNamePlugin();
        IPluginConfig config = EaseAgent.getConfig(plugin.getDomain(), plugin.getNamespace(), interceptor.getType());
        interceptor.init(config, "", "", "");
    }


    @Test
    public void init() {
        MockBaseServiceNameInterceptor mockBaseServiceNameInterceptor = new MockBaseServiceNameInterceptor();
        initInterceptor(mockBaseServiceNameInterceptor);
        ServiceNamePluginConfig serviceNamePluginConfig = mockBaseServiceNameInterceptor.getConfig();
        assertEquals(Const.DEFAULT_PROPAGATE_HEAD, serviceNamePluginConfig.getPropagateHead());
    }

    @Test
    public void order() {
        MockBaseServiceNameInterceptor mockBaseServiceNameInterceptor = new MockBaseServiceNameInterceptor();
        assertEquals(Order.HIGH.getOrder(), mockBaseServiceNameInterceptor.order());
    }

    @Test
    public void getType() {
        MockBaseServiceNameInterceptor mockBaseServiceNameInterceptor = new MockBaseServiceNameInterceptor();
        assertEquals("addServiceNameHead", mockBaseServiceNameInterceptor.getType());
    }

    static class MockBaseServiceNameInterceptor extends BaseServiceNameInterceptor {

        @Override
        public void before(MethodInfo methodInfo, Context context) {

        }

        public ServiceNamePluginConfig getConfig() {
            return config;
        }
    }
}
