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

package com.megaease.easeagent.plugin.jdbc.interceptor.tracing;

import com.megaease.easeagent.mock.plugin.api.MockEaseAgent;
import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.mock.plugin.api.utils.InterceptorTestUtils;
import com.megaease.easeagent.mock.plugin.api.utils.SpanTestUtils;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.RedirectProcessor;
import com.megaease.easeagent.plugin.api.middleware.Type;
import com.megaease.easeagent.plugin.api.trace.Span;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.jdbc.JdbcTracingPlugin;
import com.megaease.easeagent.plugin.jdbc.TestUtils;
import com.megaease.easeagent.plugin.jdbc.common.SQLCompressionFactory;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;
import com.megaease.easeagent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.SQLException;

import static org.junit.Assert.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JdbcStmTracingInterceptorTest {

    @Test
    public void init() {
        JdbcStmTracingInterceptor interceptor = new JdbcStmTracingInterceptor();
        InterceptorTestUtils.init(interceptor, new JdbcTracingPlugin());
        assertNotNull(AgentFieldReflectAccessor.getStaticFieldValue(JdbcStmTracingInterceptor.class, "md5SQLCompression"));
    }

    @Test
    public void doBefore() throws SQLException {
        JdbcStmTracingInterceptor interceptor = new JdbcStmTracingInterceptor();
        Context context = EaseAgent.getOrCreateTracingContext();
        InterceptorTestUtils.init(interceptor, new JdbcTracingPlugin());
        String method = "test_method";
        MethodInfo methodInfo = MethodInfo.builder().method(method).build();
        interceptor.doBefore(methodInfo, context);
        assertNull(context.get(JdbcStmTracingInterceptor.SPAN_KEY));

        SqlInfo sqlInfo = new SqlInfo(TestUtils.mockConnection());
        String sql = "select * from data";
        sqlInfo.addSql(sql, false);
        context.put(SqlInfo.class, sqlInfo);

        interceptor.doBefore(methodInfo, context);
        Span span = context.remove(JdbcStmTracingInterceptor.SPAN_KEY);
        assertNotNull(span);
        span.finish();
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, reportSpan);

        assertEquals(method, reportSpan.name());
        assertEquals(Span.Kind.CLIENT.name(), reportSpan.kind());
        assertEquals(SQLCompressionFactory.getSqlCompression().compress(sql), reportSpan.tag(JdbcStmTracingInterceptor.SPAN_SQL_QUERY_TAG_NAME));
        assertEquals("database", reportSpan.tag(JdbcStmTracingInterceptor.SPAN_LOCAL_COMPONENT_TAG_NAME));
        assertEquals(TestUtils.URI, reportSpan.tag(JdbcStmTracingInterceptor.SPAN_URL));
        assertEquals(Type.DATABASE.getRemoteType(), reportSpan.tag(MiddlewareConstants.TYPE_TAG_NAME));
        assertEquals("mysql-" + TestUtils.DATABASE, reportSpan.remoteServiceName());
        assertEquals(TestUtils.HOST, reportSpan.remoteEndpoint().ipv4());
        assertEquals(TestUtils.PORT, reportSpan.remoteEndpoint().port());

        TestUtils.setRedirect();
        RedirectProcessor.redirected(Redirect.DATABASE, TestUtils.URI);
        interceptor.doBefore(methodInfo, context);
        span = context.remove(JdbcStmTracingInterceptor.SPAN_KEY);
        assertNotNull(span);
        span.finish();
        reportSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, reportSpan);
        assertEquals(TestUtils.URI, reportSpan.tag(MiddlewareConstants.REDIRECTED_LABEL_REMOTE_TAG_NAME));
    }

    @Test
    public void doAfter() throws SQLException {
        JdbcStmTracingInterceptor interceptor = new JdbcStmTracingInterceptor();
        InterceptorTestUtils.init(interceptor, new JdbcTracingPlugin());
        Context context = EaseAgent.getOrCreateTracingContext();
        String method = "test_method";
        MethodInfo methodInfo = MethodInfo.builder().method(method).build();
        SqlInfo sqlInfo = new SqlInfo(TestUtils.mockConnection());
        String sql = "select * from data";
        sqlInfo.addSql(sql, false);
        context.put(SqlInfo.class, sqlInfo);
        interceptor.doBefore(methodInfo, context);
        Span span = context.get(JdbcStmTracingInterceptor.SPAN_KEY);
        assertNotNull(span);
        interceptor.doAfter(methodInfo, context);
        ReportSpan reportSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, reportSpan);
        assertFalse(reportSpan.hasError());

        String error = "test error";
        methodInfo = MethodInfo.builder().method(method).throwable(new RuntimeException(error)).build();
        interceptor.doBefore(methodInfo, context);
        span = context.get(JdbcStmTracingInterceptor.SPAN_KEY);
        assertNotNull(span);
        interceptor.doAfter(methodInfo, context);
        reportSpan = MockEaseAgent.getLastSpan();
        SpanTestUtils.sameId(span, reportSpan);
        assertTrue(reportSpan.hasError());
        assertEquals(error, reportSpan.errorInfo());
    }

    @Test
    public void getType() {
        JdbcStmTracingInterceptor interceptor = new JdbcStmTracingInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

    @Test
    public void order() {
        JdbcStmTracingInterceptor interceptor = new JdbcStmTracingInterceptor();
        assertEquals(Order.TRACING_APPEND.getOrder(), interceptor.order());
    }
}
