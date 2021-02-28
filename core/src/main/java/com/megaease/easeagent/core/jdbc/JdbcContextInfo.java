package com.megaease.easeagent.core.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class JdbcContextInfo {

    private static final Logger logger = LoggerFactory.getLogger(JdbcContextInfo.class);

    private static final ThreadLocal<JdbcContextInfo> jdbcContextInfo = ThreadLocal.withInitial(JdbcContextInfo::create);

    private final Map<Statement, ExecutionInfo> infoMap = new HashMap<>();

    public static JdbcContextInfo getCurrent() {
        return jdbcContextInfo.get();
    }

    public static JdbcContextInfo create() {
        return new JdbcContextInfo();
    }

    private JdbcContextInfo() {

    }

    public ExecutionInfo getExecutionInfo(Statement statement) {
        ExecutionInfo info = infoMap.get(statement);
        if (info == null) {
            logger.warn("can not found ExecutionInfo for " + statement.getClass().getName());
        }
        return info;
    }

    public void removeByStatement(Statement statement) {
        infoMap.remove(statement);
    }

    public void updateOnCreateStatement(Connection connection, Statement statement, String sql) {
        ExecutionInfo info = new ExecutionInfo();
        info.setConnection(connection);
        if (sql != null) {
            info.addSql(sql, false);
        }
        infoMap.put(statement, info);
    }

    public void addSql(Statement statement, String sql, boolean forBatch) {
        ExecutionInfo info = getExecutionInfo(statement);
        if (info != null) {
            info.addSql(sql, forBatch);
        }
    }

    public void clearBatch(Statement statement) {
        ExecutionInfo info = getExecutionInfo(statement);
        if (info != null) {
            info.clearSql();
        }
    }

    public Map<Statement, ExecutionInfo> getInfoMap() {
        return infoMap;
    }

}
