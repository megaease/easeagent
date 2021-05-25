/*
 * Copyright (c) 2017, MegaEase
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
 */

package com.megaease.easeagent.common.jdbc;

import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;

import static org.mockito.Mockito.mock;

public class SqlInfoTest {

    @Test
    public void success() {
        Connection connection = mock(Connection.class);
        SqlInfo sqlInfo = new SqlInfo(connection);
        sqlInfo.addSql("a", false);
        sqlInfo.addSql("b", false);
        Assert.assertEquals("b", sqlInfo.getSql());
    }

    @Test
    public void success4Batch() {
        Connection connection = mock(Connection.class);
        SqlInfo sqlInfo = new SqlInfo(connection);
        sqlInfo.addSql("a", true);
        sqlInfo.addSql("b", true);
        Assert.assertEquals("a\nb", sqlInfo.getSql());
    }

    @Test
    public void success4BatchNulSql() {
        Connection connection = mock(Connection.class);
        SqlInfo sqlInfo = new SqlInfo(connection);
        sqlInfo.addSql(null, true);
        sqlInfo.addSql("b", true);
        Assert.assertEquals("null\nb", sqlInfo.getSql());
    }
}
