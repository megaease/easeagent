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
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaConsumerAdvice;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

@AdviceTo(value = KafkaConsumerAdvice.class, qualifier = "poll", plugin = KafkaPlugin.class)
public class KafkaConsumerMetricInterceptor implements NonReentrantInterceptor {
    private static KafkaMetric kafkaMetric;

    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        kafkaMetric = ServiceMetricRegistry.getOrCreate(config, KafkaMetric.newTags(), KafkaMetric.KAFKA_METRIC_SUPPLIER);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        if (!methodInfo.isSuccess()) {
            return;
        }
        ConsumerRecords<?, ?> consumerRecords = (ConsumerRecords<?, ?>) methodInfo.getRetValue();
        if (consumerRecords == null || consumerRecords.isEmpty()) {
            return;
        }
        for (ConsumerRecord<?, ?> consumerRecord : consumerRecords) {
            Timer.Context ctx = this.kafkaMetric.consumeStart(consumerRecord.topic());
            this.kafkaMetric.consumeStop(ctx, consumerRecord.topic());
        }
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

}
