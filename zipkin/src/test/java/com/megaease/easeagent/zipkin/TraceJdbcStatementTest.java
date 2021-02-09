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

 package com.megaease.easeagent.zipkin;

import brave.Tracer;
import brave.Tracing;
import com.google.common.collect.ImmutableMap;
import com.megaease.easeagent.common.CallTrace;
import com.megaease.easeagent.common.ForwardLock;
import com.megaease.easeagent.core.Classes;
import com.megaease.easeagent.core.Definition;
import com.mysql.cj.NativeSession;
import com.mysql.cj.conf.HostInfo;
import com.mysql.cj.jdbc.*;
import com.mysql.cj.protocol.a.NativeServerSession;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import zipkin2.Span;
import zipkin2.reporter.Reporter;

import java.lang.reflect.Method;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.util.Map;

import static org.mockito.Mockito.*;

@SuppressWarnings("unchecked")
public class TraceJdbcStatementTest {
    static final String JDBC_URL = "jdbc:mysql://localhost/test";

    @Test
    public void should_work() throws Exception {
        final CallTrace trace = new CallTrace();
        final Reporter<Span> reporter = mock(Reporter.class);
        final Tracer tracer = tracer(reporter);
        final ClassLoader loader = getClass().getClassLoader();
        final String name = "com.mysql.cj.jdbc.ClientPreparedStatement";

        trace.push(tracer.newTrace().start());

        final Definition.Default def = new GenTraceJdbcStatement().define(Definition.Default.EMPTY);
        Classes.transform(name)
                .with(def, trace, new ForwardLock(), tracer).load(loader);

        final ConnectionImpl conn = mock(ConnectionImpl.class, RETURNS_DEEP_STUBS);
        final DatabaseMetaData metaData = mock(DatabaseMetaData.class);
        when(metaData.getURL()).thenReturn(JDBC_URL);
        when(conn.getMetaData()).thenReturn(metaData);

        //mock server info
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
        PreparedStatement stat = (PreparedStatement) method.invoke(null, conn, "sql", "cat");

        try { stat.execute(); } catch (Exception ignore) { }

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(reporter).report(captor.capture());
        final Span span = captor.getValue();
        Assert.assertEquals(span.name(), "jdbc_statement");

        final Map<String, String> map = ImmutableMap.<String, String>builder()
                .put("component", "jdbc")
                .put("has.error", "true")
                .put("jdbc.result", "false")
                .put("jdbc.sql", "sql")
                .put("jdbc.url", JDBC_URL)
                .put("remote.address", "127.0.0.1")
                .put("remote.type", "mysql")
                .put("span.kind", "client")
                .build();
        Assert.assertEquals(span.tags(),map);
        trace.pop();
    }


    private Tracer tracer(Reporter<Span> reporter) {
        return Tracing.newBuilder().spanReporter(reporter).build().tracer();
    }

}