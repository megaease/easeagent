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

import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;

@Builder
@Data
public class DatabaseInfo {
    private String database;
    private String host;
    private int port;

    public static DatabaseInfo getFromConnection(Connection connection) {
        try {
            String jdbcURL = connection.getMetaData().getURL();
            URI url = URI.create(jdbcURL.substring(5)); // strip "jdbc:"
            String remoteServiceName;
            String databaseName = connection.getCatalog();
            if (databaseName != null && !databaseName.isEmpty()) {
                remoteServiceName = databaseName;
            } else {
                remoteServiceName = "";
            }
            return DatabaseInfo.builder()
                    .host(StringUtils.isNotEmpty(url.getHost()) ? url.getHost() : "")
                    .database(remoteServiceName)
                    .port(url.getPort() == -1 ? 3306 : url.getPort())
                    .build();
        } catch (SQLException ignored) {
        }
        return null;
    }
}
