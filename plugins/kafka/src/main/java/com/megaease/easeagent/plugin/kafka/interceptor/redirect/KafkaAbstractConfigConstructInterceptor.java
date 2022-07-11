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

package com.megaease.easeagent.plugin.kafka.interceptor.redirect;

import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.logging.Logger;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;

import java.util.Map;
import java.util.Properties;

public class KafkaAbstractConfigConstructInterceptor implements NonReentrantInterceptor {
    private static final Logger LOGGER = EaseAgent.getLogger(KafkaAbstractConfigConstructInterceptor.class);
    private static final String BOOTSRAP_CONFIG = "bootstrap.servers";

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        ResourceConfig cnf = Redirect.KAFKA.getConfig();
        if (cnf == null) {
            return;
        }
        if (methodInfo.getArgs()[0] instanceof Properties) {
            Properties properties = (Properties) methodInfo.getArgs()[0];
            LOGGER.info("Redirect Kafka uris: {} to {}", properties.getProperty(BOOTSRAP_CONFIG), cnf.getUris());
            properties.put(BOOTSRAP_CONFIG, cnf.getUris());
            methodInfo.changeArg(0, properties);
            RedirectProcessor.redirected(Redirect.KAFKA, cnf.getUris());
        } else if (methodInfo.getArgs()[0] instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) methodInfo.getArgs()[0];
            LOGGER.info("Redirect Kafka uris: {} to {}", map.get(BOOTSRAP_CONFIG), cnf.getUris());
            map.put(BOOTSRAP_CONFIG, cnf.getUris());
            methodInfo.changeArg(0, map);
            RedirectProcessor.redirected(Redirect.KAFKA, cnf.getUris());
        }
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
