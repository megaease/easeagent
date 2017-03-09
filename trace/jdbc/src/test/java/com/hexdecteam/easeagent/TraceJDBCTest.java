package com.hexdecteam.easeagent;

import com.hexdecteam.easeagent.Transformation.Feature;
import com.mysql.cj.api.jdbc.JdbcConnection;
import com.mysql.cj.jdbc.JdbcPropertySetImpl;
import net.bytebuddy.description.type.TypeDescription;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.sql.PreparedStatement;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class TraceJDBCTest {
    final Feature feature = new TraceJDBC().feature(null);

    @Test
    public void should_get_sql_from_mysql() throws Exception {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<PreparedStatement> load = Classes.<PreparedStatement>transform("com.mysql.cj.jdbc.PreparedStatement", loader)
                .by(feature).load();

        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(load)));

        final JdbcConnection connection = mock(JdbcConnection.class, RETURNS_DEEP_STUBS);
        when(connection.getPropertySet()).thenReturn(new JdbcPropertySetImpl());

        assertSqlWith(load.getConstructor(JdbcConnection.class, String.class, String.class)
                          .newInstance(connection, "sql", "cat"), "sql");
    }

    @Test
    public void should_get_sql_from_h2() throws Exception {
        final ClassLoader loader = getClass().getClassLoader();
        final Class<PreparedStatement> load = Classes.<PreparedStatement>transform("org.h2.jdbc.JdbcPreparedStatement", loader)
                .by(feature).load(loader);
        assertTrue(feature.type().matches(new TypeDescription.ForLoadedType(load)));

        final Class<org.h2.jdbc.JdbcConnection> connectionClass = org.h2.jdbc.JdbcConnection.class;
        final String sql = "select 1";

        final Constructor<?> constructor = load.getDeclaredConstructor(connectionClass, String.class, int.class, int.class, int.class, boolean.class);
        constructor.setAccessible(true);
        assertSqlWith((PreparedStatement) constructor.newInstance(mock(connectionClass, RETURNS_DEEP_STUBS), sql, 1, 1, 1, false), sql);
    }

    private void assertSqlWith(PreparedStatement s, String sql) {
        StackFrame.setRootIfAbsent("test");

        try { s.execute(); } catch (Exception ignore) { }

        final StackFrame root = StackFrame.join();
        final List<StackFrame> children = root.getChildren();

        assertThat(children.size(), is(1));

        final StackFrame frame = children.get(0);

        assertTrue(frame.getIoquery());
        assertThat(frame.getSignature(), is(sql));
    }
}