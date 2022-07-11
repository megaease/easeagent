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

import com.megaease.easeagent.plugin.jdbc.TestUtils;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatabaseInfoTest {

    @Test
    public void getFromConnection() throws SQLException {
        Connection connection = TestUtils.mockConnection();
        DatabaseInfo databaseInfo = DatabaseInfo.getFromConnection(connection);
        assertNotNull(databaseInfo);
        assertEquals(TestUtils.DATABASE, databaseInfo.getDatabase());
        assertEquals(TestUtils.HOST, databaseInfo.getHost());
        assertEquals(TestUtils.PORT, databaseInfo.getPort());
    }
}
