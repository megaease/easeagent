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

package com.megaease.easeagent.plugin.rabbitmq;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.Meter;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.MetricSubType;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class RabbitMqConsumerMetricTest {

    @Test
    public void buildOnMessageTags() {
        Tags tags = RabbitMqConsumerMetric.buildOnMessageTags();
        assertEquals("application", tags.getCategory());
        assertEquals("rabbitmq-queue", tags.getType());
        assertEquals("resource", tags.getKeyFieldName());
        assertTrue(tags.getTags().isEmpty());
        RedirectProcessor.redirected(Redirect.RABBITMQ, TestUtils.getRedirectUri());
        String testKey = "testKey";
        String testValue = "testValue";
        AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", Collections.singletonMap(testKey, testValue));

        tags = RabbitMqConsumerMetric.buildOnMessageTags();
        assertFalse(tags.getTags().isEmpty());
        assertEquals(testValue, tags.getTags().get(testKey));


        Map<Redirect, String> redirectedUris = AgentFieldReflectAccessor.getFieldValue(RedirectProcessor.INSTANCE, "redirectedUris");
        Objects.requireNonNull(redirectedUris).remove(Redirect.RABBITMQ);
    }

    @Test
    public void buildConsumerTags() {
        Tags tags = RabbitMqConsumerMetric.buildConsumerTags();
        assertEquals("application", tags.getCategory());
        assertEquals("rabbitmq-consumer", tags.getType());
        assertEquals("resource", tags.getKeyFieldName());
        assertTrue(tags.getTags().isEmpty());
        RedirectProcessor.redirected(Redirect.RABBITMQ, TestUtils.getRedirectUri());
        String testKey = "testKey";
        String testValue = "testValue";
        AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", Collections.singletonMap(testKey, testValue));

        tags = RabbitMqConsumerMetric.buildConsumerTags();
        assertFalse(tags.getTags().isEmpty());
        assertEquals(testValue, tags.getTags().get(testKey));


        Map<Redirect, String> redirectedUris = AgentFieldReflectAccessor.getFieldValue(RedirectProcessor.INSTANCE, "redirectedUris");
        Objects.requireNonNull(redirectedUris).remove(Redirect.RABBITMQ);

    }

    @Test
    public void buildNameFactory() {
        NameFactory nameFactory = RabbitMqConsumerMetric.buildNameFactory();
        String key = "testBuildNameFactory";
        assertEquals(1, nameFactory.timerNames(key).size());
        assertEquals(2, nameFactory.meterNames(key).size());
    }

    @Test
    public void metricAfter() {
        IPluginConfig config = TestUtils.getMqMetricConfig();
        RabbitMqConsumerMetric metric = EaseAgent.getOrCreateServiceMetric(config, RabbitMqConsumerMetric.buildConsumerTags(), RabbitMqConsumerMetric.SERVICE_METRIC_SUPPLIER);
        String key = "testMetricAfter";
        metric.metricAfter(key, System.currentTimeMillis() - 101, true);
        TagVerifier tagVerifier = TagVerifier.build(RabbitMqConsumerMetric.buildConsumerTags(), key);
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertTrue((int) (double) metrics.get(MetricField.MIN_EXECUTION_TIME.getField()) > 100);
        Meter meter = metric.meter(key, MetricSubType.CONSUMER);
        Meter meterError = metric.meter(key, MetricSubType.CONSUMER_ERROR);
        assertEquals(1, meter.getCount());
        assertEquals(0, meterError.getCount());

        metric.metricAfter(key, System.currentTimeMillis() - 100, false);
        assertEquals(2, meter.getCount());
        assertEquals(1, meterError.getCount());
    }
}
