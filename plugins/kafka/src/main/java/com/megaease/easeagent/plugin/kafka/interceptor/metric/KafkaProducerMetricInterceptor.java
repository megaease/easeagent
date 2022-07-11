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

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.megaease.easeagent.plugin.annotation.AdviceTo;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.interceptor.NonReentrantInterceptor;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.advice.KafkaProducerAdvice;
import com.megaease.easeagent.plugin.kafka.interceptor.AsyncCallback;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaUtils;
import org.apache.kafka.clients.producer.ProducerRecord;

@AdviceTo(value = KafkaProducerAdvice.class, qualifier = "doSend", plugin = KafkaPlugin.class)
public class KafkaProducerMetricInterceptor implements NonReentrantInterceptor {
    private static volatile KafkaMetric kafkaMetric;


    @Override
    public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
        kafkaMetric = ServiceMetricRegistry.getOrCreate(config, KafkaMetric.newTags(), KafkaMetric.KAFKA_METRIC_SUPPLIER);
    }

    @Override
    public void doBefore(MethodInfo methodInfo, Context context) {
        MetricCallback metricCallback = new MetricCallback(AsyncCallback.callback(methodInfo), KafkaUtils.getTopic((ProducerRecord) methodInfo.getArgs()[0]), kafkaMetric);
        methodInfo.changeArg(1, metricCallback);
    }

    @Override
    public void doAfter(MethodInfo methodInfo, Context context) {
        if (AsyncCallback.isAsync(AsyncCallback.callback(methodInfo))) {
            return;
        }
        processSync(methodInfo);

    }

    private void processSync(MethodInfo methodInfo) {
        ProducerRecord<?, ?> producerRecord = (ProducerRecord<?, ?>) methodInfo.getArgs()[0];
        if (!methodInfo.isSuccess()) {
            this.kafkaMetric.errorProducer(producerRecord.topic());
        }
    }

    @Override
    public String getType() {
        return Order.METRIC.getName();
    }

    @Override
    public int order() {
        return Order.METRIC.getOrder();
    }

    public static KafkaMetric getKafkaMetric() {
        return kafkaMetric;
    }
}
