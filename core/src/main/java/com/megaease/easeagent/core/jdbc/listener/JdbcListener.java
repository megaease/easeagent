package com.megaease.easeagent.core.jdbc.listener;


import com.megaease.easeagent.core.jdbc.JdbcContextInfo;

import java.sql.Connection;
import java.sql.Statement;

public interface JdbcListener {


    String METHOD_CONNECTION_CREATE_STATEMENT = "createStatement";
    String METHOD_CONNECTION_START_WITH_PREPARE = "prepare";

    String METHOD_STATEMENT_ADD_BATCH = "addBatch";
    String METHOD_STATEMENT_CLOSE = "close";
    String METHOD_STATEMENT_CLEAR_BATCH = "clearBatch";
    String METHOD_STATEMENT_START_WITH_EXECUTE = "execute";

    DefaultJdbcListener DEFAULT = new DefaultJdbcListener();

    void onConnectionCreateStatement(JdbcContextInfo jdbcContextInfo, Connection connection, Statement statement);

    void onConnectionPrepare(JdbcContextInfo jdbcContextInfo, Connection connection, Statement statement, String sql);

    void onStatementAddBatch(JdbcContextInfo jdbcContextInfo, Statement statement, String sql);

    void onStatementClearBatch(JdbcContextInfo jdbcContextInfo, Statement statement);

    void onStatementClose(JdbcContextInfo jdbcContextInfo, Statement statement);

    void onStatementExecute(JdbcContextInfo jdbcContextInfo, Statement statement, String sql);

    class DefaultJdbcListener implements JdbcListener {

        @Override
        public void onConnectionCreateStatement(JdbcContextInfo jdbcContextInfo, Connection connection, Statement statement) {
            jdbcContextInfo.updateOnCreateStatement(connection, statement, null);
        }

        @Override
        public void onConnectionPrepare(JdbcContextInfo jdbcContextInfo, Connection connection, Statement statement, String sql) {
            jdbcContextInfo.updateOnCreateStatement(connection, statement, sql);
        }

        @Override
        public void onStatementAddBatch(JdbcContextInfo jdbcContextInfo, Statement statement, String sql) {
            jdbcContextInfo.addSql(statement, sql, true);
        }

        @Override
        public void onStatementClearBatch(JdbcContextInfo jdbcContextInfo, Statement statement) {
            jdbcContextInfo.clearBatch(statement);
        }

        @Override
        public void onStatementClose(JdbcContextInfo jdbcContextInfo, Statement statement) {
            jdbcContextInfo.removeByStatement(statement);
        }

        @Override
        public void onStatementExecute(JdbcContextInfo jdbcContextInfo, Statement statement, String sql) {
            jdbcContextInfo.addSql(statement, sql, false);
        }

    }
}
