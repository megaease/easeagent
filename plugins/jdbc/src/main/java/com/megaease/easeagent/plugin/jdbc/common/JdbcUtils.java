/*
 * Copyright (c) 2021, MegaEase
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

package com.megaease.easeagent.plugin.jdbc.common;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class JdbcUtils {

    public static String getUrl(Connection con) {
        try {
            final DatabaseMetaData meta = con.getMetaData();
            final String url = meta.getURL();
            int idx = url.indexOf('?');
            if (idx == -1) {
                return url;
            }
            return url.substring(0, idx);
        } catch (SQLException ignored) {
        }
        return null;
    }
}
