/*
 * Copyright (c) 2017, MegaEase
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
 */

 package com.megaease.easeagent.requests;

import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.mysql.cj.NativeSession;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.jdbc.*;
import com.mysql.cj.protocol.a.NativeServerSession;
import org.junit.Test;

import java.lang.reflect.Method;
import java.sql.PreparedStatement;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class CaptureExecuteSqlTest {

    @Test
    public void should_capture_method() throws Exception {
        final String sql0 = "sql0";
        final String sql1 = "sql1";
        final ClassLoader loader = getClass().getClassLoader();
        final CallTrace trace = new CallTrace();

        Classes.transform("com.mysql.cj.jdbc.StatementImpl", "com.mysql.cj.jdbc.ClientPreparedStatement")
                .with(new GenCaptureExecuteSql().define(Definition.Default.EMPTY), trace)
                .load(loader)
        ;

        //mock server info
        final ConnectionImpl conn = mock(ConnectionImpl.class, RETURNS_DEEP_STUBS);
        JdbcPropertySet jdbcPropertySet = new JdbcPropertySetImpl();
        when(conn.getPropertySet()).thenReturn(jdbcPropertySet);
        HostInfo hostInfo = new HostInfo();
        NativeSession nativeSession = new NativeSession(hostInfo, jdbcPropertySet);
        nativeSession = spy(nativeSession);
        doReturn(new NativeServerSession(jdbcPropertySet)).when(nativeSession).getServerSession();
        when(conn.getSession()).thenReturn(nativeSession);

        /*
         * In mysql-connector version>=8, com.mysql.cj.PreparedStatement is deleted.
         * We can use ClientPreparedStatement for test. But ClientPreparedStatement not has public constructor.
         * We can use protected static getInstance method by reflect.
         */
        Method method = ClientPreparedStatement.class.getDeclaredMethod(
                "getInstance", JdbcConnection.class, String.class, String.class);
        method.setAccessible(true);
        PreparedStatement stat = (PreparedStatement) method.invoke(null, conn, sql0, "cat");

        Context.pushIfRootCall(trace, CaptureExecuteSqlTest.class, "should_capture_method_without_sql");
        try { stat.execute(); } catch (Exception ignore) { }
        try { stat.execute(sql1); } catch (Exception ignore) { }

        final List<Context> children = Context.pop(trace).getChildren();

        final Context context0 = children.get(0);
        assertThat(context0.getSignature(), is(sql0));
        assertThat(context0.getShortSignature(), is("ClientPreparedStatement#execute"));
        assertThat(context0.getIoquery(), is(true));

        final Context context1 = children.get(1);
        assertThat(context1.getSignature(), is(sql1));

    }

}