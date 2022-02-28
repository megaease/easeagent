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

package com.megaease.easeagent.plugin.jdbc.common;

import com.megaease.easeagent.plugin.jdbc.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.*;

public class SqlInfoTest {

    private static SqlInfo buildSqlInfo() throws SQLException {
        Connection connection = TestUtils.mockConnection();
        return new SqlInfo(connection);
    }

    @Test
    public void getConnection() throws SQLException {
        Connection connection = TestUtils.mockConnection();
        SqlInfo sqlInfo = new SqlInfo(connection);
        assertSame(connection, sqlInfo.getConnection());
    }

    @Test
    public void addSql() throws SQLException {
        SqlInfo sqlInfo = buildSqlInfo();
        String sql = "testSql";
        sqlInfo.addSql(sql, true);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(0));
        sqlInfo.addSql(sql, true);
        assertEquals(2, sqlInfo.getSqlList().size());
        assertEquals(sql, sqlInfo.getSqlList().get(1));

        String sql2 = "testSql2";
        sqlInfo.addSql(sql2, false);
        assertEquals(1, sqlInfo.getSqlList().size());
        assertEquals(sql2, sqlInfo.getSqlList().get(0));

    }

    @Test
    public void clearSql() throws SQLException {
        SqlInfo sqlInfo = buildSqlInfo();
        sqlInfo.addSql("testSql", true);
        assertFalse(sqlInfo.getSqlList().isEmpty());
        sqlInfo.clearSql();
        assertTrue(sqlInfo.getSqlList().isEmpty());
    }

    @Test
    public void getSql() throws SQLException {
        SqlInfo sqlInfo = buildSqlInfo();
        String sql = "testSql";
        String sql2 = "testSql2";
        sqlInfo.addSql(sql, true);
        sqlInfo.addSql(sql2, true);
        assertEquals(sql + "\n" + sql2, sqlInfo.getSql());

    }

    @Test
    public void getSqlList() throws SQLException {
        addSql();
    }
}
