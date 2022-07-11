/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.interceptor;

import com.megaease.easeagent.plugin.advice.CrossThreadAdvice;
import com.megaease.easeagent.plugin.advice.ReactSchedulersAdvice;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;

@AdviceTo(CrossThreadAdvice.class)
@AdviceTo(ReactSchedulersAdvice.class)
public class RunnableInterceptor implements Interceptor {
    private static final Logger logger = EaseAgent.loggerFactory.getLogger(RunnableInterceptor.class);

    @Override
    public void before(MethodInfo methodInfo, Context context) {
        try {
            Object[] args = methodInfo.getArgs();
            Runnable task = (Runnable) args[0];
            if (!context.isWrapped(task)) {
                Runnable wrap = context.wrap(task);
                methodInfo.changeArg(0, wrap);
            }
        } catch (Throwable e) {
            logger.warn("intercept method [{}] failure", methodInfo.getMethod(), e);
        }
    }

    @Override
    public String getType() {
        return Order.TRACING.getName();
    }

    @Override
    public int order() {
        return Order.TRACING.getOrder();
    }
}
