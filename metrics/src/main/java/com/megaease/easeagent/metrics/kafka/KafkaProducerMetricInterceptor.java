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

package com.megaease.easeagent.metrics.kafka;

import com.megaease.easeagent.common.ContextCons;
import com.megaease.easeagent.core.interceptor.AgentInterceptor;
import com.megaease.easeagent.core.interceptor.AgentInterceptorChain;
import com.megaease.easeagent.core.interceptor.MethodInfo;
import com.megaease.easeagent.core.utils.ContextUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Map;

public class KafkaProducerMetricInterceptor implements AgentInterceptor {

    private final KafkaMetric kafkaMetric;

    public KafkaProducerMetricInterceptor(KafkaMetric kafkaMetric) {
        this.kafkaMetric = kafkaMetric;
    }

    @Override
    public Object after(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        Boolean async = ContextUtils.getFromContext(context, ContextCons.ASYNC_FLAG);
        if (async != null && async) {
            return this.processAsync(methodInfo, context, chain);
        }
        return this.processSync(methodInfo, context, chain);
    }

    private Object processSync(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ProducerRecord<?, ?> producerRecord = (ProducerRecord<?, ?>) methodInfo.getArgs()[0];
        if (!methodInfo.isSuccess()) {
            this.kafkaMetric.errorProducer(producerRecord.topic());
        }
        return chain.doAfter(methodInfo, context);
    }

    private Object processAsync(MethodInfo methodInfo, Map<Object, Object> context, AgentInterceptorChain chain) {
        ProducerRecord<?, ?> producerRecord = (ProducerRecord<?, ?>) methodInfo.getArgs()[0];
        this.kafkaMetric.producerStop(ContextUtils.getBeginTime(context), producerRecord.topic());
        if (!methodInfo.isSuccess()) {
            this.kafkaMetric.errorProducer(producerRecord.topic());
        }
        return chain.doAfter(methodInfo, context);
    }
}
