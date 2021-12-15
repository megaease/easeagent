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

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.servicename.ReflectionTool;
import com.megaease.easeagent.plugin.servicename.advice.FeignLoadBalancerAdvice;
import com.netflix.client.config.IClientConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


@AdviceTo(value = FeignLoadBalancerAdvice.class, qualifier = "servicename")
public class FeignLoadBalancerInterceptor extends BaseServiceNameInterceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(FeignLoadBalancerInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("enter method [{}]", methodInfo.getMethod());
            }
            Object request = methodInfo.getArgs()[0];
            IClientConfig iClientConfig = (IClientConfig) methodInfo.getArgs()[1];
            String serviceName = iClientConfig.getClientName();
            Object realRequest = ReflectionTool.invokeMethod(request, "getRequest");
            Map<String, Collection<String>> headers = (Map<String, Collection<String>>) ReflectionTool.extractField(realRequest, "headers");
            headers.put(config.getPropagateHead(), Collections.singleton(serviceName));
            context.injectForwardedHeaders((name, value) -> headers.put(name, Collections.singleton(value)));
        } catch (Throwable e) {
            LOGGER.warn("intercept method [{}] failure", methodInfo.getMethod(), e);
        }
    }
}
