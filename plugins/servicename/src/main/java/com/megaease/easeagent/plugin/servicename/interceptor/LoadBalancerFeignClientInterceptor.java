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

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.servicename.ReflectionTool;
import com.megaease.easeagent.plugin.servicename.advice.LoadBalancerFeignClientAdvice;

@AdviceTo(value = LoadBalancerFeignClientAdvice.class, qualifier = "servicename")
public class LoadBalancerFeignClientInterceptor implements Interceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(LoadBalancerFeignClientInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {

    }

    @Override
    public void after(MethodInfo methodInfo, Context context) {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("exit method [{}]", methodInfo.getMethod());
            }
            Object retValue = methodInfo.getRetValue();
            Object clientName = ReflectionTool.invokeMethod(retValue, "getClientName");
            if (clientName == null) {
                clientName = methodInfo.getArgs()[1];
                ReflectionTool.invokeMethod(retValue, "setClientName", clientName);
            }
        } catch (Throwable e) {
            LOGGER.warn("intercept method [{}] failure", methodInfo.getMethod(), e);
        }
    }


    @Override
    public int order() {
        return Order.HIGH.getOrder();
    }

    @Override
    public String getName() {
        return "addServiceNameHead";
    }
}
