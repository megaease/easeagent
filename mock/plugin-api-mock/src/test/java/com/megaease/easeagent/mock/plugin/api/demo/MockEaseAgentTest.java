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

package com.megaease.easeagent.mock.plugin.api.demo;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.config.IPluginConfig;
import com.megaease.easeagent.plugin.api.metric.MetricRegistry;
import com.megaease.easeagent.plugin.api.metric.ServiceMetric;
import com.megaease.easeagent.plugin.api.metric.name.*;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import com.megaease.easeagent.plugin.utils.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class MockEaseAgentTest {

    @Test
    public void getLastSpan() {
        Span span = EaseAgent.getContext().nextSpan();
        span.start().finish();
        assertNotNull(MockEaseAgent.getLastSpan());
        SpanTestUtils.sameId(span, MockEaseAgent.getLastSpan());
    }

    @Test
    public void cleanLastSpan() {
        Span span = EaseAgent.getContext().nextSpan();
        span.start().finish();
        assertNotNull(MockEaseAgent.getLastSpan());
        SpanTestUtils.sameId(span, MockEaseAgent.getLastSpan());
        MockEaseAgent.cleanLastSpan();
        assertNull(MockEaseAgent.getLastSpan());
    }

    @Test
    public void setMockSpanReport() {
        List<ReportSpan> spans = new ArrayList<>();
        MockEaseAgent.setMockSpanReport(spans::add);
        Span span1 = EaseAgent.getContext().nextSpan();
        span1.start().finish();
        Span span2 = EaseAgent.getContext().nextSpan();
        span2.start().finish();
        assertEquals(2, spans.size());
        SpanTestUtils.sameId(span1, spans.get(0));
        SpanTestUtils.sameId(span2, spans.get(1));
    }


    @Test
    public void lastMetricJsonReporter() {
        //Interceptor init
        IPluginConfig config = EaseAgent.getConfig("observability", "lastMetricJsonReporter", ConfigConst.PluginID.METRIC);
        NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.DEFAULT,
            ImmutableMap.<MetricField, MetricValueFetcher>builder()
                .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount).build()
        ).build();
        String type = "lastMetricJsonReporterType";
        Tags tags = new Tags("testCategory", type, "testName");
        MetricRegistry metricRegistry = EaseAgent.newMetricRegistry(config, nameFactory, tags);

        //Interceptor after
        String key = "lastMetricJsonReporterKey";
        metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT)).inc();

        //verify metric count==1
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(TagVerifier.build(tags, key)::verifyAnd);
        Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));

        //reset all metric to empty.
        MockEaseAgent.resetAll();
        try {
            lastJsonReporter.flushAndOnlyOne();
            fail("must throw Exception");
        } catch (RuntimeException e) {
            //throw Exception because it is empty.
        }

        //inc 1
        metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT)).inc();
        metric = lastJsonReporter.flushAndOnlyOne();
        //count == 1
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));

        //clean all metric
        MockEaseAgent.cleanAllMetric();
        try {
            lastJsonReporter.flushAndOnlyOne();
            fail("must throw Exception");
        } catch (RuntimeException e) {
            //throw Exception because it is empty.
        }

        //inc 1
        metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT)).inc();
        metric = lastJsonReporter.flushAndOnlyOne();
        //count == 1
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));

        //clean all metric
        MockEaseAgent.cleanMetric(metricRegistry);
        try {
            lastJsonReporter.flushAndOnlyOne();
            fail("must throw Exception");
        } catch (RuntimeException e) {
            //throw Exception because it is empty.
        }

        ServiceMetric serviceMetric = new ServiceMetric(metricRegistry, nameFactory) {
        };
        serviceMetric.counter(key, MetricSubType.DEFAULT).inc();
        metric = lastJsonReporter.flushAndOnlyOne();
        //count == 1
        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));

        //clean all metric
        MockEaseAgent.cleanMetric(serviceMetric);
        try {
            lastJsonReporter.flushAndOnlyOne();
            fail("must throw Exception");
        } catch (RuntimeException e) {
            //throw Exception because it is empty.
        }

    }

    @Test
    public void getConfigs() {
        Configs configs = MockEaseAgent.getConfigs();
        String name = configs.getString("name");
        assertNotNull(name);
    }

    @Test
    public void resetAll() {
        Context context = EaseAgent.getContext();
        String key = "testKey";
        context.put(key, "value");
        assertNotNull(context.get(key));
        MockEaseAgent.resetAll();
        assertNull(context.get(key));

    }

    @Test
    public void cleanAllMetric() {
        lastMetricJsonReporter();
    }

    @Test
    public void cleanMetric() {
        lastMetricJsonReporter();
    }

    @Test
    public void clearMetric() {
        lastMetricJsonReporter();
    }
}
