package com.megaease.easeagent.core.jdbc;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class ExecutionInfo {

    private Connection connection;
    private final List<String> sqlList = new ArrayList<>();

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public void addSql(String sql, boolean forBatch) {
        if (!forBatch) {
            this.sqlList.clear();
        }
        this.sqlList.add(sql);
    }

    public void clearSql() {
        this.sqlList.clear();
    }

    public String getSql() {
        if (this.sqlList.isEmpty()) {
            return null;
        }
        return String.join("\n", this.sqlList);
    }

    public List<String> getSqlList() {
        return sqlList;
    }
}
