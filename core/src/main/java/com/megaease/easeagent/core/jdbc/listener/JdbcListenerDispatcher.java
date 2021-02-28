package com.megaease.easeagent.core.jdbc.listener;

import com.megaease.easeagent.core.jdbc.JdbcContextInfo;

import java.sql.Connection;
import java.sql.Statement;

public class JdbcListenerDispatcher {

    private final JdbcListener jdbcListener;

    public static final JdbcListenerDispatcher DEFAULT = new JdbcListenerDispatcher(JdbcListener.DEFAULT);

    public JdbcListenerDispatcher(JdbcListener jdbcListener) {
        this.jdbcListener = jdbcListener;
    }

    public void dispatchBefore(JdbcContextInfo jdbcContextInfo, Object invoker, String method, Object[] args) {
        if (invoker instanceof Statement) {
            this.dispatchStatementBefore(jdbcContextInfo, invoker, method, args);
        }
    }

    public void dispatchStatementBefore(JdbcContextInfo jdbcContextInfo, Object invoker, String method, Object[] args) {
        if (method.equals(JdbcListener.METHOD_STATEMENT_ADD_BATCH)) {
            if (args == null) {
                return;
            }
            this.jdbcListener.onStatementAddBatch(jdbcContextInfo, (Statement) invoker, (String) args[0]);
            return;
        }
        if (method.startsWith(JdbcListener.METHOD_STATEMENT_START_WITH_EXECUTE)) {
            String sql;
            if (args == null) {
                sql = null;
            } else {
                sql = (String) args[0];
            }
            if (sql != null) {
                this.jdbcListener.onStatementExecute(jdbcContextInfo, (Statement) invoker, sql);
            }
        }
    }

    public void dispatchAfter(JdbcContextInfo jdbcContextInfo, Object invoker, String method, Object[] args, Object retValue) {
        if (invoker instanceof Connection) {
            this.dispatchConnectionAfter(jdbcContextInfo, invoker, method, args, retValue);
            return;
        }
        if (invoker instanceof Statement) {
            this.dispatchStatementAfter(jdbcContextInfo, invoker, method);
        }
    }

    private void dispatchConnectionAfter(JdbcContextInfo jdbcContextInfo, Object invoker, String method, Object[] args, Object retValue) {
        if (method.equals(JdbcListener.METHOD_CONNECTION_CREATE_STATEMENT)) {
            this.jdbcListener.onConnectionCreateStatement(jdbcContextInfo, (Connection) invoker, (Statement) retValue);
            return;
        }
        if (method.startsWith(JdbcListener.METHOD_CONNECTION_START_WITH_PREPARE)) {
            this.jdbcListener.onConnectionPrepare(jdbcContextInfo, (Connection) invoker, (Statement) retValue, (String) args[0]);
        }
    }

    private void dispatchStatementAfter(JdbcContextInfo jdbcContextInfo, Object invoker, String method) {
        if (method.equals(JdbcListener.METHOD_STATEMENT_CLOSE)) {
            this.jdbcListener.onStatementClose(jdbcContextInfo, (Statement) invoker);
            return;
        }
        if (method.equals(JdbcListener.METHOD_STATEMENT_CLEAR_BATCH)) {
            this.jdbcListener.onStatementClearBatch(jdbcContextInfo, (Statement) invoker);
        }
    }

}
