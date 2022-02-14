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
import com.megaease.easeagent.mock.report.ReportMock;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.Timer;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.Interceptor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.kafka.KafkaPlugin;
import com.megaease.easeagent.plugin.kafka.interceptor.KafkaTestUtils;
import com.megaease.easeagent.plugin.kafka.interceptor.TestConst;
import com.megaease.easeagent.plugin.kafka.interceptor.redirect.KafkaAbstractConfigConstructInterceptor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@MockEaseAgent
public class KafkaMetricTest {
    public static final String TOPIC = "testTopic";

    public static KafkaMetric get() {
        KafkaPlugin kafkaPlugin = new KafkaPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(kafkaPlugin.getDomain(), kafkaPlugin.getNamespace(), ConfigConst.PluginID.METRIC);
        return ServiceMetricRegistry.getOrCreate(iPluginConfig, KafkaMetric.newTags(), KafkaMetric.KAFKA_METRIC_SUPPLIER);
    }

    public static void init(Interceptor interceptor) {
        KafkaPlugin kafkaPlugin = new KafkaPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(kafkaPlugin.getDomain(), kafkaPlugin.getNamespace(), ConfigConst.PluginID.METRIC);
        interceptor.init(iPluginConfig, "", "", "");
    }

    public static LastJsonReporter lastMetricSupplier(String topic) {
        TagVerifier tagVerifier = new TagVerifier()
            .add("category", "application")
            .add("type", "kafka")
            .add("resource", topic);
        return ReportMock.lastMetricJsonReporter(tagVerifier::verifyAnd);
    }

    public static Map<String, Object> waitOne(LastJsonReporter lastJsonReporter) {
        List<Map<String, Object>> one = lastJsonReporter.waitOne(3, TimeUnit.SECONDS);
        assertNotNull(one);
        assertEquals(1, one.size());
        return one.get(0);
    }


    @Test
    public void producerStop() {
        MockMetricUtils.clearAll();
        KafkaMetric kafkaMetric = get();
        kafkaMetric.producerStop(System.currentTimeMillis() - 100, TOPIC);
        LastJsonReporter lastJsonReporter = lastMetricSupplier(TOPIC);
        Map<String, Object> metric = waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_PRODUCER_COUNT.getField()));

    }

    @Test
    public void errorProducer() {
        MockMetricUtils.clearAll();
        KafkaMetric kafkaMetric = get();
        kafkaMetric.errorProducer(TOPIC);
        LastJsonReporter lastJsonReporter = lastMetricSupplier(TOPIC);
        Map<String, Object> metric = waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_PRODUCER_ERROR_COUNT.getField()));

    }

    @Test
    public void consumeStart() {
        MockMetricUtils.clearAll();
        KafkaMetric kafkaMetric = get();
        Timer.Context context = kafkaMetric.consumeStart(TOPIC);
        assertNotNull(context);
    }

    @Test
    public void consumeStop() {
        MockMetricUtils.clearAll();
        KafkaMetric kafkaMetric = get();
        Timer.Context context = kafkaMetric.consumeStart(TOPIC);
        kafkaMetric.consumeStop(context, TOPIC);
        LastJsonReporter lastJsonReporter = lastMetricSupplier(TOPIC);
        Map<String, Object> metric = waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_COUNT.getField()));
    }

    @Test
    public void consumeError() {
        MockMetricUtils.clearAll();
        KafkaMetric kafkaMetric = get();
        kafkaMetric.consumeError(TOPIC);
        LastJsonReporter lastJsonReporter = lastMetricSupplier(TOPIC);
        Map<String, Object> metric = waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_ERROR_COUNT.getField()));
    }

    @Test
    public void consume() {
        MockMetricUtils.clearAll();
        KafkaMetric kafkaMetric = get();
        kafkaMetric.consume(TOPIC, System.currentTimeMillis() - 100, true);
        LastJsonReporter lastJsonReporter = lastMetricSupplier(TOPIC);
        Map<String, Object> metric = waitOne(lastJsonReporter);
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_COUNT.getField()));

        kafkaMetric.consume(TOPIC, System.currentTimeMillis() - 100, false);
        lastJsonReporter.clean();

        metric = waitOne(lastJsonReporter);
        assertEquals(2, metric.get(MetricField.EXECUTION_CONSUMER_COUNT.getField()));
        assertEquals(1, metric.get(MetricField.EXECUTION_CONSUMER_ERROR_COUNT.getField()));

    }

    @Test
    public void newTags() {
        Tags tags = KafkaMetric.newTags();
        assertEquals("application", tags.getCategory());
        assertEquals("kafka", tags.getType());
        assertEquals("resource", tags.getKeyFieldName());

        KafkaAbstractConfigConstructInterceptor interceptor = new KafkaAbstractConfigConstructInterceptor();
        KafkaTestUtils.mockRedirect(() -> {
            Map config = new HashMap();
            config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, TestConst.URIS);

            MethodInfo methodInfo = MethodInfo.builder().args(new Object[]{config}).build();
            interceptor.doBefore(methodInfo, EaseAgent.getContext());

            String testTagKey = "tagKey";
            String testTagValue = "tagValue";
            Map<String, String> oldTags = RedirectProcessor.INSTANCE.getTags();
            Map<String, String> newTagsMap = new HashMap<>();
            newTagsMap.put(testTagKey, testTagValue);
            AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", newTagsMap);
            try {
                Tags newTags = KafkaMetric.newTags();
                assertEquals(testTagValue, newTags.getTags().get(testTagKey));
            } finally {
                AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", oldTags);
            }
        });

    }

    @Test
    public void nameFactory() {
        NameFactory nameFactory = KafkaMetric.nameFactory();
        assertNotNull(nameFactory);
    }
}
