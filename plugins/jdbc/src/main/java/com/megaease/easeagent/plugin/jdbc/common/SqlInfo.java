/*
 * Copyright (c) 2022, MegaEase
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
 *
 */

package com.megaease.easeagent.plugin.jdbc.common;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class SqlInfo {
    private final Connection connection;

    public SqlInfo(Connection connection) {
        this.connection = connection;
    }

    private final List<String> sqlList = new ArrayList<>();

    public Connection getConnection() {
        return connection;
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

