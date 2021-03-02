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

package com.megaease.easeagent.metrics.jdbc.interceptor;

import com.codahale.metrics.MetricRegistry;
import com.megaease.easeagent.core.jdbc.JdbcUtils;
import com.megaease.easeagent.metrics.jdbc.AbstractJdbcMetric;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

public class JdbcConMetricInterceptor extends AbstractJdbcMetric {

    public JdbcConMetricInterceptor(MetricRegistry metricRegistry) {
        super(metricRegistry);
    }

    @Override
    public void after(Object invoker, String method, Object[] args, Object retValue, Exception exception, Map<Object, Object> context) {
        Connection connection = (Connection) context.get(Connection.class);
        try {
            String key;
            boolean success = true;
            if (retValue == null) {
                key = ERR_CON_METRIC_KEY;
                success = false;
            } else {
                key = getMetricKey(connection, exception);
            }
            this.collectMetric(key, success, context);
        } catch (SQLException ignored) {
        }
    }

    private static String getMetricKey(Connection con, Exception exception) throws SQLException {
        if (exception != null) {
            return ERR_CON_METRIC_KEY;
        }
        return JdbcUtils.getUrl(con);
    }


}
