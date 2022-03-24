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

package com.megaease.easeagent.plugin.jdbc.interceptor.metric;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.TagVerifier;
import com.megaease.easeagent.mock.report.impl.LastJsonReporter;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.context.ContextUtils;
import com.megaease.easeagent.plugin.api.metric.name.MetricField;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.jdbc.JdbcConnectionMetricPlugin;
import com.megaease.easeagent.plugin.jdbc.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JdbcDataSourceMetricInterceptorTest {

    @Test
    public void init() {
        JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new JdbcConnectionMetricPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(JdbcDataSourceMetricInterceptor.class, "metric"));

    }

    @Test
    public void doBefore() {
        JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor();
        interceptor.doBefore(null, null);
        assertTrue(true);
    }

    @Test
    public void doAfter() throws SQLException {
        JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new JdbcConnectionMetricPlugin());
        Context context = EaseAgent.getOrCreateTracingContext();
        ContextUtils.setBeginTime(context);

        MethodInfo methodInfo = MethodInfo.builder().build();
        interceptor.doAfter(methodInfo, context);

        TagVerifier errorTagVerifier = TagVerifier.build(JdbcMetric.newConnectionTags(), JdbcDataSourceMetricInterceptor.ERR_CON_METRIC_KEY);
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(errorTagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

        Connection connection = TestUtils.mockConnection();
        methodInfo = MethodInfo.builder().retValue(connection).throwable(new RuntimeException("test error")).build();
        interceptor.doAfter(methodInfo, context);
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(2, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(2, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

        methodInfo = MethodInfo.builder().retValue(connection).build();
        interceptor.doAfter(methodInfo, context);
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(2, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(2, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));
        TagVerifier urlTagVerifier = TagVerifier.build(JdbcMetric.newConnectionTags(), TestUtils.URI);
        lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(urlTagVerifier::verifyAnd);
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertNull(metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

    }

    @Test
    public void getType() {
        JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }

    @Test
    public void order() {
        JdbcDataSourceMetricInterceptor interceptor = new JdbcDataSourceMetricInterceptor();
        assertEquals(Order.METRIC.getOrder(), interceptor.order());
    }
}
