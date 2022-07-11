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

package com.megaease.easeagent.plugin.jdbc;

import com.megaease.easeagent.mock.utils.MockSystemEnv;
import com.megaease.easeagent.plugin.api.middleware.MiddlewareConstants;
import com.megaease.easeagent.plugin.api.middleware.Redirect;
import com.megaease.easeagent.plugin.api.middleware.ResourceConfig;
import com.megaease.easeagent.plugin.field.AgentFieldReflectAccessor;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils {
    public static final String DATABASE = "db_damo";
    public static final String HOST = "192.168.1.14";
    public static final int PORT = 1234;
    public static final String URI = String.format("jdbc:mysql://%s:%s/%s", HOST, PORT, DATABASE);
    public static final String FULL_URI = URI + "?useUnicode=true&characterEncoding=utf-8&autoReconnectForPools=true&autoReconnect=true";
    public static final String REDIRECT_USERNAME = "testUserName";
    public static final String REDIRECT_PASSWORD = "testPassword";


    public static Connection mockConnection() throws SQLException {
        Connection connection = mock(Connection.class);
        DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(connection.getCatalog()).thenReturn(DATABASE);
        when(databaseMetaData.getURL()).thenReturn(FULL_URI);
        return connection;
    }

    public static void setRedirect() {
        MockSystemEnv.set(MiddlewareConstants.ENV_DATABASE, String.format("{\"uris\":\"%s\", \"userName\":\"%s\",\"password\":\"%s\"}", FULL_URI, REDIRECT_USERNAME, REDIRECT_PASSWORD));
        AgentFieldReflectAccessor.setFieldValue(Redirect.DATABASE, "config", ResourceConfig.getResourceConfig(Redirect.DATABASE.getEnv(), Redirect.DATABASE.isNeedParse()));
    }

//    public static void setSqlInfoToContext() throws SQLException {
//        SqlInfo sqlInfo = new SqlInfo(TestUtils.mockConnection());
//        String sql = "select * from data";
//        sqlInfo.addSql(sql, false);
//        EaseAgent.getContext().put(SqlInfo.class, sqlInfo);
//    }
}
