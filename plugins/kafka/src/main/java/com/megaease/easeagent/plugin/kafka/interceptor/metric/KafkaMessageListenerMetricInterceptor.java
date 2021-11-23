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

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.megaease.easeagent.plugin.MethodInfo;
import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.Config;
import com.megaease.easeagent.plugin.api.metric.AbstractMetric;
import com.megaease.easeagent.plugin.kafka.advice.KafkaMessageListenerAdvice;
import com.megaease.easeagent.plugin.interceptor.FirstEnterInterceptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@AdviceTo(value = KafkaMessageListenerAdvice.class)
public class KafkaMessageListenerMetricInterceptor implements FirstEnterInterceptor {
    private static final Object START = new Object();
    private static KafkaMetric kafkaMetric;

    @Override
    public void init(Config config, String className, String methodName, String methodDescriptor) {
        kafkaMetric = AbstractMetric.getInstance(config, KafkaMetric.newTags(), (config1, tags1) -> new KafkaMetric(config1, tags1));
    }


    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        context.put(START, System.currentTimeMillis());
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        ConsumerRecord<?, ?> consumerRecord = (ConsumerRecord<?, ?>) methodInfo.getArgs()[0];
        this.kafkaMetric.consume(consumerRecord.topic(), context.remove(START), methodInfo.isSuccess());
    }
}
