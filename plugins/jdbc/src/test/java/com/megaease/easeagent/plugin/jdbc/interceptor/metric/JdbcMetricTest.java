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

package com.megaease.easeagent.plugin.jdbc.interceptor.metric;

import com.google.common.cache.RemovalCause;
import com.google.common.cache.RemovalNotification;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.ServiceMetricRegistry;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.api.metric.name.NameFactory;
import com.megaease.easeagent.plugin.api.metric.name.Tags;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.jdbc.JdbcConnectionMetricPlugin;
import com.megaease.easeagent.plugin.jdbc.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JdbcMetricTest {

    public static JdbcMetric get() {
        JdbcConnectionMetricPlugin jdbcPlugin = new JdbcConnectionMetricPlugin();
        IPluginConfig iPluginConfig = EaseAgent.getConfig(jdbcPlugin.getDomain(), jdbcPlugin.getNamespace(), ConfigConst.PluginID.METRIC);
        return ServiceMetricRegistry.getOrCreate(iPluginConfig, JdbcMetric.newConnectionTags(), JdbcMetric.METRIC_SUPPLIER);
    }

    @Test
    public void newConnectionTags() {
        Tags tags = JdbcMetric.newConnectionTags();
        assertEquals("application", tags.getCategory());
        assertEquals("jdbc-connection", tags.getType());
        assertEquals("url", tags.getKeyFieldName());

        TestUtils.setRedirect();
        RedirectProcessor.redirected(Redirect.DATABASE, TestUtils.URI);

        String testTagKey = "tagKey";
        String testTagValue = "tagValue";
        Map<String, String> oldTags = RedirectProcessor.INSTANCE.getTags();
        Map<String, String> newTagsMap = new HashMap<>();
        newTagsMap.put(testTagKey, testTagValue);
        AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", newTagsMap);
        Tags newTags = JdbcMetric.newConnectionTags();
        assertEquals(testTagValue, newTags.getTags().get(testTagKey));

    }

    @Test
    public void newStmTags() {
        Tags tags = JdbcMetric.newStmTags();
        assertEquals("application", tags.getCategory());
        assertEquals("jdbc-statement", tags.getType());
        assertEquals("signature", tags.getKeyFieldName());

        TestUtils.setRedirect();
        RedirectProcessor.redirected(Redirect.DATABASE, TestUtils.URI);
        String testTagKey = "tagKey";
        String testTagValue = "tagValue";
        Map<String, String> newTagsMap = new HashMap<>();
        newTagsMap.put(testTagKey, testTagValue);
        AgentFieldReflectAccessor.setFieldValue(RedirectProcessor.INSTANCE, "tags", newTagsMap);
        Tags newTags = JdbcMetric.newStmTags();
        assertEquals(testTagValue, newTags.getTags().get(testTagKey));
    }

    @Test
    public void nameFactory() {
        NameFactory nameFactory = JdbcMetric.nameFactory();
        assertEquals(4, nameFactory.metricTypes().size());
    }

    @Test
    public void collectMetric() {
        JdbcMetric jdbcMetric = get();
        Context context = EaseAgent.getContext();
        ContextUtils.setBeginTime(context);
        jdbcMetric.collectMetric(TestUtils.URI, true, context);
        TagVerifier tagVerifier = TagVerifier.build(JdbcMetric.newConnectionTags(), TestUtils.URI);
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        Object errorCount = metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField());
        if (errorCount != null) {
            assertEquals(0, (int) (double) errorCount);
        }

        jdbcMetric.collectMetric(TestUtils.URI, false, context);
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(2, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

    }

    @Test
    public void onRemoval() {
        JdbcMetric jdbcMetric = get();
        Context context = EaseAgent.getContext();
        ContextUtils.setBeginTime(context);
        jdbcMetric.collectMetric(TestUtils.URI, true, context);
        TagVerifier tagVerifier = TagVerifier.build(JdbcMetric.newConnectionTags(), TestUtils.URI);
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertNotNull(metrics);
        jdbcMetric.collectMetric(TestUtils.URI, false, context);
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertNotNull(metrics);

        RemovalNotification<String, String> removalNotification = RemovalNotification.create(TestUtils.URI, "", RemovalCause.SIZE);
        jdbcMetric.onRemoval(removalNotification);
        try {
            lastJsonReporter.flushAndOnlyOne();
            fail("must be throw error");
        } catch (Exception e) {
            //must be error
        }

    }
}
