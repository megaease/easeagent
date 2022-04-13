/*
 * Copyright (c) 2021, MegaEase
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.megaease.easeagent.plugin.kafka.interceptor.metric;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class KafkaMessageListenerMetricInterceptorTest {

    @Test
    public void init() {
        KafkaMessageListenerMetricInterceptor interceptor = new KafkaMessageListenerMetricInterceptor();
        KafkaMetricTest.init(interceptor);
        assertNotNull(KafkaMessageListenerMetricInterceptor.getKafkaMetric());
    }

    @Test
    public void doBefore() {
        KafkaMessageListenerMetricInterceptor interceptor = new KafkaMessageListenerMetricInterceptor();
        Context context = EaseAgent.getContext();
        context.remove(KafkaMessageListenerMetricInterceptor.START);

        interceptor.doBefore(null, context);

        assertNotNull(context.get(KafkaMessageListenerMetricInterceptor.START));

        context.remove(KafkaMessageListenerMetricInterceptor.START);
    }

    @Test
    public void doAfter() {

        KafkaMessageListenerMetricInterceptor interceptor = new KafkaMessageListenerMetricInterceptor();
        KafkaMetricTest.init(interceptor);

        Context context = EaseAgent.getContext();
        context.remove(KafkaMessageListenerMetricInterceptor.START);

        interceptor.doBefore(null, context);
        String topic = KafkaMessageListenerMetricInterceptorTest.class.getName() + ".doAfter";
        ConsumerRecord<String, String> record = KafkaConsumerMetricInterceptorTest.record(topic, 0);

        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{record}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());

        LastJsonReporter lastJsonReporter = KafkaMetricTest.lastMetricSupplier(topic);
        lastJsonReporter.clean();
        Map<String, Object> metric = KafkaMetricTest.waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_COUNT.getField()));

        interceptor.doBefore(null, context);
        methodInfo = MethodInfo.builder().args(new Object[]{record}).throwable(new RuntimeException("error")).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        lastJsonReporter.clean();
        metric = KafkaMetricTest.waitOne(lastJsonReporter);
        assertEquals(2, metric.get(MetricField.EXECUTION_CONSUMER_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_ERROR_COUNT.getField()));
    }


    @Test
    public void getType() {
        KafkaMessageListenerMetricInterceptor interceptor = new KafkaMessageListenerMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }
}
