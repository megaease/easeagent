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

package com.megaease.easeagent.plugin.jdbc.interceptor;

import com.megaease.easeagent.mock.plugin.api.junit.EaseAgentJunit4ClassRunner;
import com.megaease.easeagent.plugin.api.Context;
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.jdbc.MockJDBCStatement;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JdbcStmPrepareSqlInterceptorTest {

    @Test
    public void doBefore() {
        JdbcStmPrepareSqlInterceptor interceptor = new JdbcStmPrepareSqlInterceptor();
        Context context = EaseAgent.getContext();
        MockJDBCStatement mockJDBCStatement = mock(MockJDBCStatement.class);
        SqlInfo sqlInfo = new SqlInfo(null);
        when(mockJDBCStatement.getEaseAgent$$DynamicField$$Data()).thenReturn(sqlInfo);
        String sql = "select * from data";
        MethodInfo methodInfo = MethodInfo.builder().invoker(mockJDBCStatement).method("addBatch").args(new Object[]{sql}).build();
        interceptor.doBefore(methodInfo, context);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));
        interceptor.doBefore(methodInfo, context);
        assertEquals(2, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));

        methodInfo = MethodInfo.builder().invoker(mockJDBCStatement).method("clearBatch").build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(sqlInfo.getSqlList().isEmpty());

        methodInfo = MethodInfo.builder().invoker(mockJDBCStatement).method("execute").args(new Object[]{sql}).build();
        interceptor.doBefore(methodInfo, context);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));
        interceptor.doBefore(methodInfo, context);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));

        methodInfo = MethodInfo.builder().invoker(mockJDBCStatement).method("clearBatch").args(new Object[]{null}).build();
        interceptor.doBefore(methodInfo, context);
        assertTrue(sqlInfo.getSqlList().isEmpty());

        assertSame(sqlInfo, context.get(SqlInfo.class));

    }

    @Test
    public void getType() {
        JdbcStmPrepareSqlInterceptor interceptor = new JdbcStmPrepareSqlInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());
    }

    @Test
    public void order() {
        JdbcStmPrepareSqlInterceptor interceptor = new JdbcStmPrepareSqlInterceptor();
        assertEquals(Order.HIGH.getOrder(), interceptor.order());
    }
}
