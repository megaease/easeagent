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

package com.megaease.easeagent.plugin.redis.interceptor.redirect;

import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.redis.RedisRedirectPlugin;
import com.megaease.easeagent.plugin.redis.advice.RedisPropertiesClusterAdvice;

@AdviceTo(value = RedisPropertiesClusterAdvice.class, plugin = RedisRedirectPlugin.class)
public class RedisPropertiesClusterSetNodesInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(RedisPropertiesClusterSetNodesInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.REDIS.getConfig();
        if (cnf == null) {
            return;
        }
        LOGGER.info("Redirect Redis uris {} to {}", methodInfo.getArgs()[0], cnf.getUris());
        methodInfo.changeArg(0, cnf.getUris());
        RedirectProcessor.redirected(Redirect.REDIS, cnf.getUris());
    }

    @Override
    public String getType() {
        return Order.REDIRECT.getName();
    }

    @Override
    public int order() {
        return Order.REDIRECT.getOrder();
    }
}
