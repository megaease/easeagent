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

package com.megaease.easeagent.plugin.rabbitmq.v5.interceptor.redirect;

import com.megaease.easeagent.plugin.Interceptor;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.rabbitmq.RabbitMqRedirectPlugin;
import com.megaease.easeagent.plugin.rabbitmq.v5.advice.RabbitMqConfigFactoryAdvice;

@AdviceTo(value = RabbitMqConfigFactoryAdvice.class, plugin = RabbitMqRedirectPlugin.class)
public class RabbitMqConfigFactoryInterceptor implements Interceptor {
    @Override
    public void before(MethodInfo methodInfo, Context context) {
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
