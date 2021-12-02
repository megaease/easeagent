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

package com.megaease.easeagent.plugin.kafka.interceptor.initialize;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.kafka.advice.KafkaProducerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaUtils;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.util.Map;

@AdviceTo(value = KafkaProducerAdvice.class, qualifier = "constructor")
public class KafkaProducerConstructInterceptor implements FirstEnterInterceptor {

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        Object invoker = methodInfo.getInvoker();
        Map<String, Object> configs = (Map<String, Object>) methodInfo.getArgs()[0];
        Object bootstrapServers = configs.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG);
        String uri = KafkaUtils.getUri(bootstrapServers);
        AgentDynamicFieldAccessor.setDynamicFieldValue(invoker, uri);
    }

    @Override
    public String getName() {
        return Order.INIT.getName();
    }
}
