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
import com.megaease.easeagent.plugin.api.config.ConfigConst;
import com.megaease.easeagent.plugin.bridge.EaseAgent;
import com.megaease.easeagent.plugin.enums.Order;
import com.megaease.easeagent.plugin.field.AgentDynamicFieldAccessor;
import com.megaease.easeagent.plugin.interceptor.MethodInfo;
import com.megaease.easeagent.plugin.jdbc.MockJDBCStatement;
import com.megaease.easeagent.plugin.jdbc.TestUtils;
import com.megaease.easeagent.plugin.jdbc.common.SqlInfo;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(EaseAgentJunit4ClassRunner.class)
public class JdbConPrepareOrCreateStmInterceptorTest {

    @Test
    public void doBefore() {
        JdbConPrepareOrCreateStmInterceptor interceptor = new JdbConPrepareOrCreateStmInterceptor();
        interceptor.doBefore(null, null);
        assertTrue(true);
    }

    @Test
    public void doAfter() throws SQLException {
        JdbConPrepareOrCreateStmInterceptor interceptor = new JdbConPrepareOrCreateStmInterceptor();
        Statement statement = mock(Statement.class);
        Connection connection = TestUtils.mockConnection();
        MethodInfo methodInfo = MethodInfo.builder().invoker(connection).retValue(statement).method("").build();
        interceptor.doBefore(methodInfo, EaseAgent.getContext());

        MockJDBCStatement mockJDBCStatement = mock(MockJDBCStatement.class);
        doCallRealMethod().when(mockJDBCStatement).setEaseAgent$$DynamicField$$Data(any());
        doCallRealMethod().when(mockJDBCStatement).getEaseAgent$$DynamicField$$Data();
        methodInfo = MethodInfo.builder().invoker(connection).retValue(mockJDBCStatement).method("").build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        SqlInfo sqlInfo = AgentDynamicFieldAccessor.getDynamicFieldValue(mockJDBCStatement);
        assertNotNull(sqlInfo);
        assertTrue(sqlInfo.getSqlList().isEmpty());


        mockJDBCStatement.setEaseAgent$$DynamicField$$Data(null);
        String sql = "select * from data";
        methodInfo = MethodInfo.builder().invoker(connection).retValue(mockJDBCStatement)
            .method("prepareA").args(new Object[]{sql}).build();
        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        sqlInfo = AgentDynamicFieldAccessor.getDynamicFieldValue(mockJDBCStatement);
        assertNotNull(sqlInfo);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));

        interceptor.doAfter(methodInfo, EaseAgent.getContext());
        sqlInfo = AgentDynamicFieldAccessor.getDynamicFieldValue(mockJDBCStatement);
        assertNotNull(sqlInfo);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));
    }

    @Test
    public void getType() {
        JdbConPrepareOrCreateStmInterceptor interceptor = new JdbConPrepareOrCreateStmInterceptor();
        assertEquals(ConfigConst.PluginID.TRACING, interceptor.getType());

    }

    @Test
    public void order() {
        JdbConPrepareOrCreateStmInterceptor interceptor = new JdbConPrepareOrCreateStmInterceptor();
        assertEquals(Order.HIGHEST.getOrder(), interceptor.order());
    }
}
