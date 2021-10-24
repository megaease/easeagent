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

package com.megaease.easeagent.zipkin.kafka.v2d3;

import brave.Tracing;
import com.megaease.easeagent.common.config.SwitchUtil;
import com.megaease.easeagent.config.Config;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.zipkin.kafka.brave.KafkaTracing;
import com.megaease.easeagent.zipkin.kafka.brave.TracingConsumer;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.util.Map;

public class KafkaConsumerTracingInterceptor implements AgentInterceptor {

    public static final String ENABLE_KEY = "observability.tracings.kafka.enabled";
    private final KafkaTracing kafkaTracing;
    private final Config config;

    public KafkaConsumerTracingInterceptor(Tracing tracing, Config config) {
        this.kafkaTracing = KafkaTracing.newBuilder(tracing).remoteServiceName("kafka").build();
        this.config = config;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        if (!methodInfo.isSuccess()) {
            return chain.doAfter(methodInfo, context);
        }
        if (!SwitchUtil.enableTracing(config, ENABLE_KEY)) {
            return chain.doAfter(methodInfo, context);
        }
        Consumer<?, ?> consumer = (Consumer<?, ?>) methodInfo.getInvoker();
        String uri = AgentDynamicFieldAccessor.getDynamicFieldValue(consumer);
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        TracingConsumer<?, ?> tracingConsumer = kafkaTracing.consumer(consumer);
        tracingConsumer.afterPoll(consumerRecords, span -> span.tag("kafka.broker", uri));
        return chain.doAfter(methodInfo, context);
    }
}
