package com.megaease.easeagent.core.jdbc;

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
