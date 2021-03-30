package com.megaease.easeagent.common.jdbc;

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
