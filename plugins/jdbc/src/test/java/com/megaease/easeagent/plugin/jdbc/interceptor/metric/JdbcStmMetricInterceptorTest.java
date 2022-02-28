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
import com.megaease.easeagent.plugin.jdbc.JdbcDataSourceMetricPlugin;
import com.megaease.easeagent.plugin.jdbc.TestUtils;
import com.megaease.easeagent.plugin.jdbc.common.MD5SQLCompression;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JdbcStmMetricInterceptorTest {

    @Test
    public void init() {
        JdbcStmMetricInterceptor interceptor = new JdbcStmMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new JdbcDataSourceMetricPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(JdbcStmMetricInterceptor.class, "metric"));
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(JdbcStmMetricInterceptor.class, "sqlCompression"));
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(JdbcStmMetricInterceptor.class, "cache"));
    }

    @Test
    public void doBefore() {
        JdbcStmMetricInterceptor interceptor = new JdbcStmMetricInterceptor();
        interceptor.doBefore(null, null);
        assertTrue(true);
    }

    @Test
    public void doAfter() throws SQLException {
        JdbcStmMetricInterceptor interceptor = new JdbcStmMetricInterceptor();
        InterceptorTestUtils.init(interceptor, new JdbcDataSourceMetricPlugin());

        Context context = EaseAgent.getContext();
        ContextUtils.setBeginTime(context);
        SqlInfo sqlInfo = new SqlInfo(TestUtils.mockConnection());
        String sql = "select * from data";
        sqlInfo.addSql(sql, false);
        context.put(SqlInfo.class, sqlInfo);

        String key = MD5SQLCompression.getInstance().compress(sqlInfo.getSql());
        MethodInfo methodInfo = MethodInfo.builder().build();
        interceptor.doAfter(methodInfo, context);
        TagVerifier tagVerifier = TagVerifier.build(JdbcMetric.newStmTags(), key);
        LastJsonReporter lastJsonReporter = MockEaseAgent.lastMetricJsonReporter(tagVerifier::verifyAnd);
        Map<String, Object> metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(1, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        Object errorCount = metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField());
        if (errorCount != null) {
            assertEquals(0, (int) (double) errorCount);
        }

        methodInfo = MethodInfo.builder().throwable(new RuntimeException("test error")).build();
        interceptor.doAfter(methodInfo, context);
        metrics = lastJsonReporter.flushAndOnlyOne();
        assertEquals(2, metrics.get(MetricField.EXECUTION_COUNT.getField()));
        assertEquals(1, metrics.get(MetricField.EXECUTION_ERROR_COUNT.getField()));

    }

    @Test
    public void getType() {
        JdbcStmMetricInterceptor interceptor = new JdbcStmMetricInterceptor();
        assertEquals(ConfigConst.PluginID.METRIC, interceptor.getType());
    }

    @Test
    public void order() {
        JdbcStmMetricInterceptor interceptor = new JdbcStmMetricInterceptor();
        assertEquals(Order.METRIC.getOrder(), interceptor.order());
    }
}
