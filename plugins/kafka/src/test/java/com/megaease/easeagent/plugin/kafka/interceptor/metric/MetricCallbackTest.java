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
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MetricCallbackTest {

    @Test
    public void onCompletion() {

        KafkaMetric kafkaMetric = KafkaMetricTest.get();

        String topic = "testTopic";
        MetricCallback metricCallback = new MetricCallback(null, topic, kafkaMetric);
        metricCallback.onCompletion(null, null);

        LastJsonReporter lastJsonReporter = KafkaMetricTest.lastMetricSupplier(topic);
        Map<String, Object> metric = KafkaMetricTest.waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_PRODUCER_COUNT.getField()));

        metricCallback.onCompletion(null, new RuntimeException("test"));

        lastJsonReporter.clean();
        metric = KafkaMetricTest.waitOne(lastJsonReporter);
        assertEquals(2, metric.get(MetricField.EXECUTION_PRODUCER_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_PRODUCER_ERROR_COUNT.getField()));

        AtomicBoolean ran = new AtomicBoolean(false);
        metricCallback = new MetricCallback((metadata, exception) -> ran.set(true), topic, kafkaMetric);
        metricCallback.onCompletion(null, null);
        assertTrue(ran.get());
    }
}
