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

package com.megaease.easeagent.mock.plugin.api;

import com.megaease.easeagent.config.Configs;
import com.megaease.easeagent.mock.config.MockConfig;
import com.megaease.easeagent.mock.metrics.MetricTestUtils;
import com.megaease.easeagent.mock.metrics.MockMetricProvider;
import com.megaease.easeagent.mock.plugin.api.utils.ContextUtils;
import com.megaease.easeagent.mock.report.MockReport;
import com.megaease.easeagent.mock.report.MockSpanReport;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.metric.ServiceMetric;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Mock EaseAgent for unit test
 *
 * <pre>${@code
 * @RunWith(EaseAgentJunit4ClassRunner.class)
 * public class InterceptorTest {
 *     @Test
 *     public void testBeforeAfter() {
 *         assertNull(MockEaseAgent.getLastSpan());
 *         final Object key = new Object();
 *         Interceptor interceptor = new Interceptor() {
 *             @Override
 *             public void before(MethodInfo methodInfo, Context context) {
 *                 Span span = context.nextSpan();
 *                 span.start();
 *                 context.put(key, span);
 *             }
 *
 *             @Override
 *             public void after(MethodInfo methodInfo, Context context) {
 *                 Span span = context.remove(key);
 *                 span.finish();
 *             }
 *         };
 *         Context context = EaseAgent.getContext();
 *         interceptor.before(null, context);
 *         Span span = context.get(key);
 *         assertNotNull(span);
 *         assertNull(MockEaseAgent.getLastSpan());
 *         interceptor.after(null, context);
 *         ReportSpan reportSpan = MockEaseAgent.getLastSpan();
 *         SpanTestUtils.sameId(span, reportSpan);
 *     }
 * }
 * }</pre>
 */
public class MockEaseAgent {

    /**
     * get last Span from Report cache
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter#getLastSpan
     *
     * <pre>${@code
     *         Span span = EaseAgent.getContext().nextSpan();
     *         span.start().finish();
     *         assertNotNull(MockEaseAgent.getLastSpan());
     *         SpanTestUtils.sameId(span, MockEaseAgent.getLastSpan());
     * }</pre>
     *
     * @return Report Span
     */
    public static ReportSpan getLastSpan() {
        return MockReport.getLastSpan();
    }

    /**
     * clean last Span cache from Report
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter#cleanLastSpan
     *
     * <pre>${@code
     *         Span span = EaseAgent.getContext().nextSpan();
     *         span.start().finish();
     *         assertNotNull(MockEaseAgent.getLastSpan());
     *         SpanTestUtils.sameId(span, MockEaseAgent.getLastSpan());
     *         MockEaseAgent.cleanLastSpan();
     *         assertNull(MockEaseAgent.getLastSpan());
     * }</pre>
     */
    public static void cleanLastSpan() {
        MockReport.cleanLastSpan();
    }

    /**
     * set MockSpanReport for receive all finish Span
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter#setMockSpanReport
     *
     * <pre>${@code
     *         List<ReportSpan> spans = new ArrayList<>();
     *         MockEaseAgent.setMockSpanReport(spans::add);
     *         Span span1 = EaseAgent.getContext().nextSpan();
     *         span1.start().finish();
     *         Span span2 = EaseAgent.getContext().nextSpan();
     *         span2.start().finish();
     *         assertEquals(2, spans.size());
     *         SpanTestUtils.sameId(span1, spans.get(0));
     *         SpanTestUtils.sameId(span2, spans.get(1));
     * }</pre>
     *
     * @param mockSpanReport {@link MockSpanReport}
     */
    public static void setMockSpanReport(MockSpanReport mockSpanReport) {
        MockReport.setMockSpanReport(mockSpanReport);
    }


    /**
     * create and get ${@link LastJsonReporter}  for metric by filter
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter#lastMetricJsonReporter
     *
     * <pre>${@code
     *         //Interceptor init
     *         IPluginConfig config = EaseAgent.getConfig("observability", "lastMetricJsonReporter", ConfigConst.PluginID.METRIC);
     *         NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.DEFAULT,
     *             ImmutableMap.<MetricField, MetricValueFetcher>builder()
     *                 .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount).build()
     *         ).build();
     *         String type = "lastMetricJsonReporterType";
     *         Tags tags = new Tags("testCategory", type, "testName");
     *         MetricRegistry metricRegistry = EaseAgent.newMetricRegistry(config, nameFactory, tags);
     *
     *         //Interceptor after
     *         String key = "lastMetricJsonReporterKey";
     *         metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT)).inc();
     *
     *         //verify metric count==1
     *         LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(TagVerifier.build(tags, key)::verifyAnd);
     *         Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
     *         assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
     * }</pre>
     *
     * @param filter
     * @return LastJsonReporter
     */
    public static LastJsonReporter lastMetricJsonReporter(Predicate<Map<String, Object>> filter) {
        return MockReport.lastMetricJsonReporter(filter);
    }

    /**
     * get all Configs for unit test
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter#getConfigs
     *
     * <pre>${@code
     *         Configs configs = MockEaseAgent.getConfigs();
     *         String name = configs.getString("name");
     *         assertNotNull(name);
     * }</pre>
     *
     * @return
     */
    public static Configs getConfigs() {
        return MockConfig.getCONFIGS();
    }

    /**
     * reset all of context: metric, tracing, redirect, etc.
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter
     *
     * <pre>${@code
     *          Context context = EaseAgent.getContext();
     *         String key = "testKey";
     *         context.put(key, "value");
     *         assertNotNull(context.get(key));
     *         MockEaseAgent.resetAll();
     *         assertNull(context.get(key));
     *
     * //        //verify metric count==1
     * //        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(TagVerifier.build(tags, key)::verifyAnd);
     * //        Map<String, Object> metric = lastJsonReporter.flushAndOnlyOne();
     * //        assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
     * //
     * //        //reset all metric to empty.
     * //        MockEaseAgent.resetAll();
     * //        try {
     * //            lastJsonReporter.flushAndOnlyOne();
     * //            fail("must throw Exception");
     * //        } catch (RuntimeException e) {
     * //            //throw Exception because it is empty.
     * //        }
     *  }</pre>
     */
    public static void resetAll() {
        ContextUtils.resetAll();
    }

    /**
     * clean all metric
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter
     *
     * <pre>${@code
     *         //inc 1
     *         metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT)).inc();
     *         metric = lastJsonReporter.flushAndOnlyOne();
     *         //count == 1
     *         assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
     *
     *         //clean all metric
     *         MockEaseAgent.cleanAllMetric();
     *         try {
     *             lastJsonReporter.flushAndOnlyOne();
     *             fail("must throw Exception");
     *         } catch (RuntimeException e) {
     *             //throw Exception because it is empty.
     *         }
     * }</pre>
     */
    public static void cleanAllMetric() {
        MockMetricProvider.clearAll();
    }

    /**
     * clean MetricRegistry's metrics
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter
     * <pre>${@code
     *         //inc 1
     *         metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT)).inc();
     *         metric = lastJsonReporter.flushAndOnlyOne();
     *         //count == 1
     *         assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
     *
     *         //clean all metric
     *         MockEaseAgent.cleanMetric(metricRegistry);
     *         try {
     *             lastJsonReporter.flushAndOnlyOne();
     *             fail("must throw Exception");
     *         } catch (RuntimeException e) {
     *             //throw Exception because it is empty.
     *         }
     * }</pre>
     *
     * @param metricRegistry ${@link com.megaease.easeagent.plugin.api.metric.MetricRegistry}
     */
    public static void cleanMetric(com.megaease.easeagent.plugin.api.metric.MetricRegistry metricRegistry) {
        MetricTestUtils.clear(metricRegistry);
    }

    /**
     * clean ServiceMetric's metrics
     * <p>
     * see com.megaease.easeagent.mock.plugin.api.damo.MockEaseAgentTest#lastMetricJsonReporter
     *
     * <pre>${@code
     *         ServiceMetric serviceMetric = new ServiceMetric(metricRegistry, nameFactory) {
     *         };
     *         serviceMetric.counter(key, MetricSubType.DEFAULT).inc();
     *         metric = lastJsonReporter.flushAndOnlyOne();
     *         //count == 1
     *         assertEquals(1, metric.get(MetricField.EXECUTION_COUNT.getField()));
     *
     *         //clean all metric
     *         MockEaseAgent.cleanMetric(serviceMetric);
     *         try {
     *             lastJsonReporter.flushAndOnlyOne();
     *             fail("must throw Exception");
     *         } catch (RuntimeException e) {
     *             //throw Exception because it is empty.
     *         }
     * }</pre>
     *
     * @param serviceMetric ${@link ServiceMetric }
     */
    public static void cleanMetric(ServiceMetric serviceMetric) {
        MetricTestUtils.clear(serviceMetric);
    }
}
