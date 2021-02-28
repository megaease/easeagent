package com.megaease.easeagent.core;

import com.megaease.easeagent.core.jdbc.ExecutionInfo;
import com.megaease.easeagent.core.jdbc.JdbcContextInfo;
import com.megaease.easeagent.core.jdbc.listener.JdbcListener;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JdbcListenerTest {

    private final JdbcListener jdbcListener = JdbcListener.DEFAULT;
    JdbcContextInfo jdbcContextInfo;

    @After
    public void after() {
        Assert.assertTrue(jdbcContextInfo.getInfoMap().isEmpty());
    }

    @Test
    public void prepare() {
        jdbcContextInfo = JdbcContextInfo.create();
        Connection connection = mock(Connection.class);

        String sql1 = "sql1";
        PreparedStatement ps1 = mock(PreparedStatement.class);
        jdbcListener.onConnectionPrepare(jdbcContextInfo, connection, ps1, sql1);
        ExecutionInfo executionInfoFromStm = jdbcContextInfo.getExecutionInfo(ps1);
        Assert.assertNotNull(executionInfoFromStm);
        String retSql = executionInfoFromStm.getSql();
        Assert.assertEquals(sql1, retSql);
        Assert.assertEquals(jdbcContextInfo.getInfoMap().size(), 1);

        String sql2 = "sql2";
        PreparedStatement ps2 = mock(PreparedStatement.class);
        jdbcListener.onConnectionPrepare(jdbcContextInfo, connection, ps2, sql2);
        executionInfoFromStm = jdbcContextInfo.getExecutionInfo(ps2);
        String retSql2 = executionInfoFromStm.getSql();
        Assert.assertEquals(sql2, retSql2);
        Assert.assertEquals(jdbcContextInfo.getInfoMap().size(), 2);

        // close statement
        jdbcListener.onStatementClose(jdbcContextInfo, ps1);
        jdbcListener.onStatementClose(jdbcContextInfo, ps2);

    }

    @Test
    public void statement() {
        jdbcContextInfo = JdbcContextInfo.create();
        Connection connection = mock(Connection.class);

        Statement ps1 = mock(Statement.class);
        jdbcListener.onConnectionCreateStatement(jdbcContextInfo, connection, ps1);
        ExecutionInfo executionInfoFromStm = jdbcContextInfo.getExecutionInfo(ps1);
        Assert.assertNotNull(executionInfoFromStm);
        Assert.assertNull(executionInfoFromStm.getSql());
        Assert.assertEquals(jdbcContextInfo.getInfoMap().size(), 1);

        Statement ps2 = mock(Statement.class);
        jdbcListener.onConnectionCreateStatement(jdbcContextInfo, connection, ps2);
        executionInfoFromStm = jdbcContextInfo.getExecutionInfo(ps2);
        Assert.assertNotNull(executionInfoFromStm);
        Assert.assertNull(executionInfoFromStm.getSql());
        Assert.assertEquals(jdbcContextInfo.getInfoMap().size(), 2);

        // batch
        jdbcListener.onStatementAddBatch(jdbcContextInfo, ps2, "sql1");
        jdbcListener.onStatementAddBatch(jdbcContextInfo, ps2, "sql2");
        Assert.assertEquals(executionInfoFromStm.getSqlList().size(), 2);
        Assert.assertEquals("sql1\nsql2", executionInfoFromStm.getSql());

        // clearBatch
        jdbcListener.onStatementClearBatch(jdbcContextInfo, ps2);
        Assert.assertNull(executionInfoFromStm.getSql());
        Assert.assertTrue(executionInfoFromStm.getSqlList().isEmpty());

        // close statement
        jdbcListener.onStatementClose(jdbcContextInfo, ps1);
        jdbcListener.onStatementClose(jdbcContextInfo, ps2);

        Assert.assertNull(jdbcContextInfo.getExecutionInfo(ps2));
        Assert.assertNull(jdbcContextInfo.getExecutionInfo(ps1));
    }

    @Test
    public void statement_fail() {
        jdbcContextInfo = JdbcContextInfo.create();
        Connection connection = mock(Connection.class);

        String sql = "select * from user;";
        try {
            when(connection.prepareStatement(sql)).thenThrow(SQLException.class);
            when(connection.createStatement()).thenThrow(SQLException.class);
        } catch (SQLException ignored) {

        }
        try {
            connection.prepareStatement(sql);
            Assert.fail("must fail for prepareStatement");
        } catch (SQLException exception) {
            Assert.assertTrue(jdbcContextInfo.getInfoMap().isEmpty());
        }
        try {
            connection.createStatement();
            Assert.fail("must fail for createStatement");
        } catch (SQLException exception) {
            Assert.assertTrue(jdbcContextInfo.getInfoMap().isEmpty());
        }
    }
}
