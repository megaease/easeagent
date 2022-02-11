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

import com.megaease.easeagent.mock.metrics.MockMetricUtils;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

@MockEaseAgent
public class KafkaProducerMetricInterceptorTest {


    @Test
    public void init() {
        KafkaProducerMetricInterceptor interceptor = new KafkaProducerMetricInterceptor();
        KafkaMetricTest.init(interceptor);
        assertNotNull(KafkaProducerMetricInterceptor.getKafkaMetric());
    }

    @Test
    public void doBefore() {
        KafkaProducerMetricInterceptor interceptor = new KafkaProducerMetricInterceptor();
        KafkaMetricTest.init(interceptor);

        String topic = KafkaProducerMetricInterceptorTest.class + ".doBefore";
        ProducerRecord record = new ProducerRecord<>(topic, "", "");
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{record, null}).build();
        interceptor.doBefore(methodInfo, null);
        assertNotNull(methodInfo.getArgs()[1]);
        assertTrue(methodInfo.getArgs()[1] instanceof MetricCallback);


    }

    @Test
    public void doAfter() {
        MockMetricUtils.clearAll();

        KafkaProducerMetricInterceptor interceptor = new KafkaProducerMetricInterceptor();
        KafkaMetricTest.init(interceptor);

        String topic = KafkaProducerMetricInterceptorTest.class + ".doAfter";
        ProducerRecord record = new ProducerRecord<>(topic, "", "");
        MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{record, null}).build();
        interceptor.doBefore(methodInfo, null);
        interceptor.doAfter(methodInfo, null);

        methodInfo.throwable(new RuntimeException("test error"));
        interceptor.doAfter(methodInfo, null);

        LastJsonReporter lastJsonReporter = KafkaMetricTest.lastMetricSupplier(topic);
        Map<String, Object> metric = KafkaMetricTest.waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_PRODUCER_ERROR_COUNT.getField()));
    }

    @Test
    public void getType() {
        KafkaProducerMetricInterceptor interceptor = new KafkaProducerMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }
}
